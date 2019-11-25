package com.labs64.netlicensing.gateway.integrations.fastspring;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.Token;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.EntityUtils;
import com.labs64.netlicensing.gateway.bl.TimeStampTracker;
import com.labs64.netlicensing.gateway.integrations.common.BaseException;
import com.labs64.netlicensing.gateway.integrations.common.BaseIntegration;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;
import com.labs64.netlicensing.service.TokenService;

@Component
public class FastSpring extends BaseIntegration {

    static final class FastSpringConstants {
        static final String NEXT_CLEANUP_TAG = "FastSpringNextCleanup";
        static final int PERSIST_PURCHASE_DAYS = 3;

        static final String CUSTOM_PROPERTY_KEY = "fastSpringUserData";
        static final String ENDPOINT_BASE_PATH = "fastspring";
        static final String SECURITY_REQUEST_HASH = "security_request_hash";
        static final String API_KEY = "apiKey";
        static final String PRIVATE_KEY = "privateKey";
        static final String QUANTITY = "quantity";
        static final String REFERENCE = "reference";
        static final String LICENSE_TEMPLATE_LIST = "licenseTemplateList";
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FastSpring.class);

    @Autowired
    private TimeStampTracker timeStampTracker;

    @Autowired
    private FastSpringPurchaseRepository fastSpringPurchaseRepository;

    public String codeGenerator(final Context context, final String reference, final String productNumber,
            final List<String> licenseTemplateList, final boolean quantityToLicensee, final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) throws NetLicensingException {

        logEntries(productNumber, reference, licenseTemplateList, formParams, LOGGER);

        final List<String> licensees = new ArrayList<>();
        if (formParams.isEmpty() || licenseTemplateList.isEmpty()) {
            throw new BaseException("Required parameters not provided");
        }
        final String licenseeNumber = formParams.getFirst(Constants.NetLicensing.LICENSEE_NUMBER);
        if (quantityToLicensee && !StringUtils.isEmpty(licenseeNumber)) {
            throw new BaseException("'" + Constants.NetLicensing.LICENSEE_NUMBER + "' is not allowed in '"
                    + Constants.QUANTITY_TO_LICENSEE + "' mode");
        }
        final String quantity = formParams.getFirst(FastSpring.FastSpringConstants.QUANTITY);
        if (quantity == null || quantity.isEmpty() || Integer.parseInt(quantity) < 1) {
            throw new BaseException("'" + FastSpring.FastSpringConstants.QUANTITY + "' invalid or not provided");
        }

        final Product product = ProductService.get(context, productNumber);
        final Map<String, LicenseTemplate> licenseTemplates = EntityUtils.getLicenseTemplates(context,
                licenseTemplateList);
        Licensee licensee = new LicenseeImpl();
        boolean isNeedCreateNewLicensee = true;

        // try to get existing Licensee
        if (!quantityToLicensee) {
            licensee = getExistingLicensee(context, licenseeNumber, reference, productNumber);
            // if license template and licensee are bound to different products, need to create new licensee
            isNeedCreateNewLicensee = isNeedCreateNewLicensee(licensee, productNumber);
        }

        // create licenses
        for (int i = 1; i <= Integer.parseInt(quantity); i++) {
            // create new Licensee, if not existing
            if (licensee == null || isNeedCreateNewLicensee || quantityToLicensee) {
                isNeedCreateNewLicensee = false;
                licensee = createLicensee(context, product, (isSaveUserData ? formParams : null));
            }
            createLicenseForLicenseTemplates(context, licenseTemplates, licensee);

            if (!licensees.contains(licensee.getNumber())) {
                licensees.add(licensee.getNumber());
            }
        }
        if (!quantityToLicensee) {
            persistPurchaseLicenseeMapping(licensee.getNumber(), reference, productNumber);
            removeExpiredPurchaseLicenseeMappings();
        }
        return "\n" + StringUtils.join(licensees, "\n");
    }

    @Override
    protected Licensee createLicensee(Context context, Product product, final MultivaluedMap<String, String> formParams)
            throws NetLicensingException {
        Licensee licensee = new LicenseeImpl();
        if (formParams != null) {
            licensee.addProperty(FastSpring.FastSpringConstants.CUSTOM_PROPERTY_KEY,
                    convertFormParamsToJson(formParams));
        }
        licensee.setActive(true);
        licensee.setProduct(product);
        licensee.addProperty(Constants.NetLicensing.PROP_MARKED_FOR_TRANSFER, "true");// todo: need?
        return LicenseeService.create(context, product.getNumber(), licensee);
    }

    private Licensee getExistingLicensee(final Context context, String licenseeNumber, final String reference,
            final String productNumber) throws NetLicensingException {
        Licensee licensee = null;
        if (StringUtils.isBlank(licenseeNumber)) { // licenseeNumber is not provided, get from database
            final FastSpringPurchase fastSpringPurchase = fastSpringPurchaseRepository.findFirstByReferenceAndProductNumber(
                    reference, productNumber);
            if (fastSpringPurchase != null) {
                licenseeNumber = fastSpringPurchase.getLicenseeNumber();
                LOGGER.info("licenseeNumber obtained from repository: {}", licenseeNumber);
            }
        }
        if (StringUtils.isNotBlank(licenseeNumber)) {
            licensee = LicenseeService.get(context, licenseeNumber);
        }
        return licensee;
    }

    /**
     * Sort all parameters passed from our system to your system by parameter name (ordinal value sorting).
     * Concatenate the value of each parameter into a single string. Ignore the parameter named "security_request_hash"
     * when generating this string. Append the private key value to the end of the string and generate an MD5 hex digest
     * on the final combined string. The resulting MD5 digest should be identical to the value passed in the parameter
     * named "security_request_hash".
     */
    public boolean isPrivateKeyValid(Context context, MultivaluedMap<String, String> formParams)
            throws NetLicensingException {
        final String securityRequestHash = formParams.getFirst(FastSpring.FastSpringConstants.SECURITY_REQUEST_HASH);

        // get token
        Token token = TokenService.get(context, context.getApiKey());
        // get private key prop and compare with stored on FastSpring
        String privateKey = token.getProperties().get(FastSpring.FastSpringConstants.PRIVATE_KEY);

        StringBuilder data = new StringBuilder();
        SortedSet<String> keys = new TreeSet<>(formParams.keySet());
        for (String key : keys) {
            String value = formParams.getFirst(key);
            if (!key.equals(FastSpring.FastSpringConstants.SECURITY_REQUEST_HASH)) {
                data.append(value.replace("\\", ""));
            }
        }
        String hashParam = data.append(privateKey).toString();
        return md5Custom(hashParam).equals(securityRequestHash);
    }

    private static String md5Custom(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // do nothing
        }

        BigInteger bigInt = new BigInteger(1, digest);
        StringBuilder md5Hex = new StringBuilder(bigInt.toString(16));

        while (md5Hex.length() < 32) {
            md5Hex.insert(0, "0");
        }

        return md5Hex.toString();
    }

    private void persistPurchaseLicenseeMapping(final String licenseeNumber, final String reference,
            final String productNumber) {
        FastSpringPurchase fastSpringPurchase = fastSpringPurchaseRepository.findFirstByReferenceAndProductNumber(
                reference, productNumber);
        if (fastSpringPurchase == null) {
            fastSpringPurchase = new FastSpringPurchase();
            fastSpringPurchase.setLicenseeNumber(licenseeNumber);
            fastSpringPurchase.setReference(reference);
            fastSpringPurchase.setProductNumber(productNumber);
        }
        fastSpringPurchase.setTimestamp(new Date());
        fastSpringPurchaseRepository.save(fastSpringPurchase);
    }

    private void removeExpiredPurchaseLicenseeMappings() {
        if (timeStampTracker.isTimeOutExpired(FastSpring.FastSpringConstants.NEXT_CLEANUP_TAG,
                Constants.CLEANUP_PERIOD_MINUTES)) {
            final Calendar earliestPersistTime = Calendar.getInstance();
            earliestPersistTime.add(Calendar.DATE, -FastSpring.FastSpringConstants.PERSIST_PURCHASE_DAYS);
            fastSpringPurchaseRepository.deleteByTimestampBefore(earliestPersistTime.getTime());
        }
    }

}
