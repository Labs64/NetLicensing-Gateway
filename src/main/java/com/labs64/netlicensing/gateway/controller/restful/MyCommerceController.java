package com.labs64.netlicensing.gateway.controller.restful;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.print.attribute.standard.Severity;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.domain.entity.License;
import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.impl.LicenseImpl;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.LicenseType;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.controller.restful.exception.MyCommerceException;
import com.labs64.netlicensing.gateway.domain.entity.Log;
import com.labs64.netlicensing.gateway.domain.entity.MyCommercePurchase;
import com.labs64.netlicensing.gateway.domain.repositories.LogRepository;
import com.labs64.netlicensing.gateway.domain.repositories.MyCommercePurchaseRepository;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseService;
import com.labs64.netlicensing.service.LicenseTemplateService;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;

@Produces({ MediaType.TEXT_PLAIN })
@Path("/" + Constants.MyCommerce.ENDPOINT_BASE_PATH)
public class MyCommerceController extends AbstractBaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCommerceController.class);

    @Inject
    private MyCommercePurchaseRepository myCommercePurchaseRepository;

    @Inject
    private LogRepository logRepository;

    @POST
    @Path("/" + Constants.MyCommerce.ENDPOINT_PATH_CODEGEN + "/{" + Constants.MyCommerce.PRODUCT_NUMBER + "}")
    @Transactional
    public String codeGenerator(@PathParam(Constants.MyCommerce.PRODUCT_NUMBER) final String productNumber,
            @QueryParam(Constants.MyCommerce.LICENSE_TEMPLATE_NUMBER) final List<String> licenseTemplateList,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.MULTIPLE_LICENSEE) final boolean multipleLicenseeMode,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.SAVE_USER_DATA) final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) {
        final String purchaseId = formParams.getFirst(Constants.MyCommerce.PURCHASE_ID);
        try {
            final Context context = getSecurityHelper().getContext();

            logRequest(productNumber, licenseTemplateList, formParams);

            final List<String> licensees = new ArrayList<String>();
            final String quantity = formParams.getFirst(Constants.MyCommerce.QUANTITY);
            final String licenseeNumber = formParams.getFirst(Constants.MyCommerce.LICENSEE_NUMBER);
            if (formParams.isEmpty() || licenseTemplateList.isEmpty()) {
                final String errorMessage = "Required parameters not provided";
                logException(errorMessage, purchaseId);
                throw new MyCommerceException(errorMessage);
            } else if (multipleLicenseeMode && licenseeNumber != null && !licenseeNumber.isEmpty()) {
                final String errorMessage = "Wrong configuration! Multiple Licensee mode is on, LICENSEENUMBER is passed";
                logException(errorMessage, purchaseId);
                throw new MyCommerceException(
                        errorMessage);
            } else if (quantity == null || quantity.isEmpty() || Integer.parseInt(quantity) < 1) {
                final String errorMessage = "Quantity is wrong";
                logException(errorMessage, purchaseId);
                throw new MyCommerceException(errorMessage);
            }

            final Product product = ProductService.get(context, productNumber);
            final Map<String, LicenseTemplate> licenseTemplates = getLicenseTemplates(context, licenseTemplateList);
            Licensee licensee = new LicenseeImpl();
            boolean isNeedCreateNewLicensee = true;

            // try to get existing Licensee
            if (!multipleLicenseeMode) {
                licensee = getExistingLicensee(context, licenseeNumber, purchaseId, productNumber);
                // if license template and licensee are bound to different products, need to create new licensee
                isNeedCreateNewLicensee = isNeedCreateNewLicensee(licensee, productNumber);
            }

            // create licenses
            for (int i = 1; i <= Integer.parseInt(quantity); i++) {
                // create new Licensee, if not existing or multipleLicenseeMode
                if (licensee == null || isNeedCreateNewLicensee || multipleLicenseeMode) {
                    isNeedCreateNewLicensee = false;
                    licensee = new LicenseeImpl();
                    if (isSaveUserData) {
                        addCustomPropertiesToLicensee(formParams, licensee);
                    }
                    licensee.setActive(true);
                    licensee.setProduct(product);
                    licensee = LicenseeService.create(context, productNumber, licensee);
                }
                for (final LicenseTemplate licenseTemplate : licenseTemplates.values()) {
                    final License newLicense = new LicenseImpl();
                    newLicense.setActive(true);
                    // Required for timeVolume.
                    if (LicenseType.TIMEVOLUME.equals(licenseTemplate.getLicenseType())) {
                        newLicense.addProperty(Constants.PROP_START_DATE, "now");
                    }
                    LicenseService.create(context, licensee.getNumber(), licenseTemplate.getNumber(), null, newLicense);
                }
                if (!licensees.contains(licensee.getNumber())) {
                    licensees.add(licensee.getNumber());
                }
            }
            if (!multipleLicenseeMode) {
                persistPurchaseLicenseeMapping(licensee.getNumber(), purchaseId, productNumber);
                removeExpiredPurchaseLicenseeMappings();
            }
            return String.join(", ", licensees);
        } catch (final NetLicensingException e) {
            logException(e.getMessage(), purchaseId);
            throw new MyCommerceException(e.getMessage());
        } catch (final Exception e) {
            logException(e.getMessage(), purchaseId);
            throw new MyCommerceException(e.getMessage());
        }
    }

    @GET
    @Path("/" + Constants.MyCommerce.ENDPOINT_PATH_ERROR_LOG)
    public String logging(@QueryParam(Constants.Monitoring.PURCHASE_ID) final String purchaseId) {
        final Iterable<Log> logs = logRepository.findByKey(purchaseId);

        final StringBuilder logStringBuilder = new StringBuilder();
        int index = 0;
        for (final Log log : logs) {
            index++;
            logStringBuilder.append(index + ". ");
            logStringBuilder.append(log.getTimestamp() + ", ");
            logStringBuilder.append("Severity: " + log.getSeverity() + ", ");
            logStringBuilder.append("Message: " + log.getMessage());
            logStringBuilder.append("\n");
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

    private void persistPurchaseLicenseeMapping(final String licenseeNumber, final String purchaseId,
            final String productNumber) {
        MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository
                .findFirstByPurchaseIdAndProductNumber(purchaseId, productNumber);
        if (myCommercePurchase == null) {
            myCommercePurchase = new MyCommercePurchase();
            myCommercePurchase.setLicenseeNumber(licenseeNumber);
            myCommercePurchase.setPurchaseId(purchaseId);
            myCommercePurchase.setProductNumber(productNumber);
        }
        myCommercePurchase.setTimestamp(new Date());
        myCommercePurchaseRepository.save(myCommercePurchase);
    }

    private void removeExpiredPurchaseLicenseeMappings() {
        if (isTimeOutExpired(Constants.MyCommerce.NEXT_CLEANUP_TAG, Constants.MyCommerce.CLEANUP_PERIOD_MINUTES)) {
            final Calendar earliestPersistTime = Calendar.getInstance();
            earliestPersistTime.add(Calendar.DATE, -Constants.MyCommerce.PERSIST_PURCHASE_DAYS);
            myCommercePurchaseRepository.deleteByTimestampBefore(earliestPersistTime.getTime());
        }
    }

    private void addCustomPropertiesToLicensee(final MultivaluedMap<String, String> formParams,
            final Licensee licensee) {
        for (final Map.Entry<String, List<String>> entry : formParams.entrySet()) {
            if (!LicenseeImpl.getReservedProps().contains(entry.getKey()) && !entry.getValue().get(0).equals("")) {
                licensee.addProperty(entry.getKey(), entry.getValue().get(0));
            }
        }
    }

    private Map<String, LicenseTemplate> getLicenseTemplates(final Context context,
            final List<String> licenseTemplateList)
                    throws MyCommerceException, NetLicensingException {
        final Map<String, LicenseTemplate> licenseTemplates = new HashMap<String, LicenseTemplate>();
        final Iterator<String> licenseTemplateIterator = licenseTemplateList.iterator();
        while (licenseTemplateIterator.hasNext()) {
            final LicenseTemplate licenseTemplate = LicenseTemplateService.get(context,
                    licenseTemplateIterator.next());
            licenseTemplates.put(licenseTemplate.getNumber(), licenseTemplate);
        }
        return licenseTemplates;
    }

    private Licensee getExistingLicensee(final Context context, String licenseeNumber, final String purchaseId,
            final String productNumber)
                    throws MyCommerceException, NetLicensingException {
        Licensee licensee = null;
        if (StringUtils.isBlank(licenseeNumber)) { // ADD[LICENSEENUMBER] is not provided, get from database
            final MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository
                    .findFirstByPurchaseIdAndProductNumber(purchaseId, productNumber);
            if (myCommercePurchase != null) {
                licenseeNumber = myCommercePurchase.getLicenseeNumber();
                LOGGER.info("licenseeNumber obtained from repository: " + licenseeNumber);
            }
        }
        if (StringUtils.isNotBlank(licenseeNumber)) {
            licensee = LicenseeService.get(context, licenseeNumber);
        }
        return licensee;
    }

    private void logRequest(final String productNumber, final List<String> licenseTemplateList,
            final MultivaluedMap<String, String> formParams) {

        final StringBuilder logStringBuilder = new StringBuilder();
        logStringBuilder.append("MyCommerce Code Generator was started! With Product number: " + productNumber
                + ", licenseTemplateList: " + licenseTemplateList.toString() + ", formParams: "
                + formParams.toString());

        LOGGER.info(logStringBuilder.toString());

        final Log requestResponse = new Log();
        requestResponse.setKey(formParams.getFirst(Constants.MyCommerce.PURCHASE_ID));
        requestResponse.setSeverity(Severity.REPORT);
        requestResponse.setMessage(logStringBuilder.toString());
        requestResponse.setTimestamp(new Date());
        logRepository.save(requestResponse);
        removeExpiredErrorLogs();
    }

    private void logException(final String message, final String purchaseId) {

        final Log requestResponse = new Log();
        requestResponse.setKey(purchaseId);
        requestResponse.setSeverity(Severity.ERROR);
        requestResponse.setMessage(message);
        requestResponse.setTimestamp(new Date());
        logRepository.save(requestResponse);
        removeExpiredErrorLogs();
    }

    private void removeExpiredErrorLogs() {
        if (isTimeOutExpired(Constants.MyCommerce.NEXT_ERROR_LOG_CLEANUP_TAG,
                Constants.MyCommerce.CLEANUP_PERIOD_MINUTES)) {
            final Calendar earliestPersistTime = Calendar.getInstance();
            earliestPersistTime.add(Calendar.DATE, -Constants.MyCommerce.PERSIST_ERROR_LOG_DAYS);
            logRepository.deleteByTimestampBefore(earliestPersistTime.getTime());
        }
    }

}
