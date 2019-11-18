package com.labs64.netlicensing.gateway.integrations.fastspring;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

import com.labs64.netlicensing.domain.entity.License;
import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.Token;
import com.labs64.netlicensing.domain.entity.impl.LicenseImpl;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.LicenseType;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.PersistingLogger;
import com.labs64.netlicensing.gateway.domain.entity.StoredLog;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseService;
import com.labs64.netlicensing.service.LicenseTemplateService;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;
import com.labs64.netlicensing.service.TokenService;

@Component
public class FastSpring {
    // TODO: licenseTemplateList - do we need to use it or use single one
    // TODO: quantityToLicensee - should we use it

    static final class FastSpringConstants {
        static final String ENDPOINT_BASE_PATH = "fastspring";
        static final String ENDPOINT_PATH_CODEGEN = "codegen";
        static final String ENDPOINT_PATH_LOG = "log";
        static final String SECURITY_REQUEST_HASH = "security_request_hash";
        static final String API_KEY = "apiKey";
        static final String PRIVATE_KEY = "privateKey";
        static final String QUANTITY = "quantity";
        static final String REFERENCE = "reference";
        static final String SAVE_USER_DATA = "saveUserData";
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FastSpring.class);

    @Autowired
    private PersistingLogger persistingLogger;

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

    public String codeGenerator(final Context context, final String reference,
            final MultivaluedMap<String, String> formParams) throws NetLicensingException {
        final List<String> licensees = new ArrayList<>();

        final boolean isSaveUserData = Boolean.parseBoolean(
                formParams.getFirst(FastSpring.FastSpringConstants.SAVE_USER_DATA));
        final String productNumber = formParams.getFirst(Constants.NetLicensing.PRODUCT_NUMBER);
        final String licenseTemplateNumber = formParams.getFirst(Constants.NetLicensing.LICENSE_TEMPLATE_NUMBER);

        final String logMessage =
                "Executing FastSpring Code Generator for productNumber: " + productNumber + ", licenseTemplateNumber: "
                        + licenseTemplateNumber + ", formParams: " + formParams.toString();
        persistingLogger.log(productNumber, reference, StoredLog.Severity.INFO, logMessage, LOGGER);

        final String quantity = formParams.getFirst(FastSpring.FastSpringConstants.QUANTITY);
        if (quantity == null || quantity.isEmpty() || Integer.parseInt(quantity) < 1) {
            throw new FastSpringException("'" + FastSpring.FastSpringConstants.QUANTITY + "' invalid or not provided");
        }

        final Product product = ProductService.get(context, productNumber);
        final LicenseTemplate licenseTemplate = LicenseTemplateService.get(context, licenseTemplateNumber);

        boolean isNeedCreateNewLicensee = true;
        Licensee licensee = new LicenseeImpl();
        final String licenseeNumber = formParams.getFirst(Constants.NetLicensing.LICENSEE_NUMBER);
        if (!StringUtils.isEmpty(licenseeNumber)) {
            licensee = LicenseeService.get(context, licenseeNumber);
            // if license template and licensee are bound to different products, need to create new licensee
            isNeedCreateNewLicensee = isNeedCreateNewLicensee(licensee, productNumber);
        }

        // create licenses
        for (int i = 1; i <= Integer.parseInt(quantity); i++) {
            // create new Licensee, if not existing
            if (isNeedCreateNewLicensee) {
                isNeedCreateNewLicensee = false;
                licensee = new LicenseeImpl();
                if (isSaveUserData) {
                    addCustomPropertiesToLicensee(formParams, licensee);
                }
                licensee.setActive(true);
                licensee.setProduct(product);
                licensee = LicenseeService.create(context, productNumber, licensee);
            }
            final License newLicense = new LicenseImpl();
            newLicense.setActive(true);
            // Required for timeVolume.
            if (LicenseType.TIMEVOLUME.equals(licenseTemplate.getLicenseType())) {
                newLicense.addProperty(Constants.NetLicensing.PROP_START_DATE, "now");
            }
            LicenseService.create(context, licensee.getNumber(), licenseTemplate.getNumber(), null, newLicense);

            if (!licensees.contains(licensee.getNumber())) {
                licensees.add(licensee.getNumber());
            }
        }
        return StringUtils.join(licensees, "\n");
    }

    public String getErrorLog(final Context context, final String productNumber) throws NetLicensingException {
        ProductService.get(context, productNumber);// dummy request

        List<StoredLog> logs;

        logs = persistingLogger.getLogsByKey(productNumber);

        final StringBuilder logStringBuilder = new StringBuilder();
        if (logs.isEmpty()) {
            logStringBuilder.append("No log entires for ");
            logStringBuilder.append(Constants.NetLicensing.PRODUCT_NUMBER);
            logStringBuilder.append("=");
            logStringBuilder.append(productNumber);
            logStringBuilder.append(" within last ");
            logStringBuilder.append(Constants.LOG_PERSIST_DAYS);
            logStringBuilder.append(" days.");
        } else {
            for (final StoredLog log : logs) {
                logStringBuilder.append(log.getTimestamp());
                logStringBuilder.append(" ");
                logStringBuilder.append(log.getSeverity());
                logStringBuilder.append(" ");
                logStringBuilder.append(log.getMessage());
                logStringBuilder.append("\n");
            }
        }
        return logStringBuilder.toString();
    }

    private boolean isNeedCreateNewLicensee(final Licensee licensee, final String productNumber) {
        boolean isNeedCreateNewLicensee = false;
        if (licensee != null) {
            if (!licensee.getProduct().getNumber().equals(productNumber)) {
                isNeedCreateNewLicensee = true;
            }
        } else {
            isNeedCreateNewLicensee = true;
        }
        return isNeedCreateNewLicensee;
    }

    private void addCustomPropertiesToLicensee(final MultivaluedMap<String, String> formParams,
            final Licensee licensee) {
        for (final Map.Entry<String, List<String>> entry : formParams.entrySet()) {
            if (!LicenseeImpl.getReservedProps().contains(entry.getKey()) && !entry.getValue().get(0).equals("")) {
                licensee.addProperty(entry.getKey(), entry.getValue().get(0));
            }
        }
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
}
