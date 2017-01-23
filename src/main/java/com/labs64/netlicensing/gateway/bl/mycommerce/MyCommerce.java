package com.labs64.netlicensing.gateway.bl.mycommerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.labs64.netlicensing.domain.entity.License;
import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.impl.LicenseImpl;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.LicenseType;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.PersistingLogger;
import com.labs64.netlicensing.gateway.bl.TimeStampTracker;
import com.labs64.netlicensing.gateway.controller.restful.exception.MyCommerceException;
import com.labs64.netlicensing.gateway.domain.entity.Log;
import com.labs64.netlicensing.gateway.domain.entity.MyCommercePurchase;
import com.labs64.netlicensing.gateway.domain.repositories.MyCommercePurchaseRepository;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseService;
import com.labs64.netlicensing.service.LicenseTemplateService;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;

@Component
public class MyCommerce {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCommerce.class);

    @Inject
    private PersistingLogger persistingLogger;

    @Inject
    private TimeStampTracker timeStampTracker;

    @Inject
    private MyCommercePurchaseRepository myCommercePurchaseRepository;

    public String codeGenerator(final Context context, final String purchaseId, final String productNumber,
            final List<String> licenseTemplateList, final boolean multipleLicenseeMode, final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) throws NetLicensingException {

        persistingLogger.logRequest(productNumber, licenseTemplateList, formParams);

        final List<String> licensees = new ArrayList<>();
        final String quantity = formParams.getFirst(Constants.MyCommerce.QUANTITY);
        final String licenseeNumber = formParams.getFirst(Constants.MyCommerce.LICENSEE_NUMBER);
        if (formParams.isEmpty() || licenseTemplateList.isEmpty()) {
            final String errorMessage = "Required parameters not provided";
            persistingLogger.logException(errorMessage, purchaseId);
            throw new MyCommerceException(errorMessage);
        } else if (multipleLicenseeMode && licenseeNumber != null && !licenseeNumber.isEmpty()) {
            final String errorMessage = "Wrong configuration! Multiple Licensee mode is on, LICENSEENUMBER is passed";
            persistingLogger.logException(errorMessage, purchaseId);
            throw new MyCommerceException(errorMessage);
        } else if (quantity == null || quantity.isEmpty() || Integer.parseInt(quantity) < 1) {
            final String errorMessage = "Quantity is wrong";
            persistingLogger.logException(errorMessage, purchaseId);
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
        return StringUtils.join(licensees, ", ");
    }

    public String getErrorLog(final String purchaseId) {
        final Iterable<Log> logs = persistingLogger.getLogsByKey(purchaseId);

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
        if (timeStampTracker.isTimeOutExpired(Constants.MyCommerce.NEXT_CLEANUP_TAG,
                Constants.MyCommerce.CLEANUP_PERIOD_MINUTES)) {
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
            final List<String> licenseTemplateList) throws MyCommerceException, NetLicensingException {
        final Map<String, LicenseTemplate> licenseTemplates = new HashMap<>();
        final Iterator<String> licenseTemplateIterator = licenseTemplateList.iterator();
        while (licenseTemplateIterator.hasNext()) {
            final LicenseTemplate licenseTemplate = LicenseTemplateService.get(context, licenseTemplateIterator.next());
            licenseTemplates.put(licenseTemplate.getNumber(), licenseTemplate);
        }
        return licenseTemplates;
    }

    private Licensee getExistingLicensee(final Context context, String licenseeNumber, final String purchaseId,
            final String productNumber) throws MyCommerceException, NetLicensingException {
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

}
