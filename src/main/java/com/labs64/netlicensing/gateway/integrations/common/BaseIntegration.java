package com.labs64.netlicensing.gateway.integrations.common;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.labs64.netlicensing.domain.entity.License;
import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.impl.LicenseImpl;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.LicenseType;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.EntityUtils;
import com.labs64.netlicensing.gateway.bl.PersistingLogger;
import com.labs64.netlicensing.gateway.domain.entity.StoredLog;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseService;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;

public abstract class BaseIntegration {

    @Autowired
    private PersistingLogger persistingLogger;

    public String getErrorLog(final Context context, final String productNumber, final String secondKey,
            String secondKeyName) throws NetLicensingException {
        ProductService.get(context, productNumber);// dummy request

        List<StoredLog> logs;

        if (secondKey != null && !secondKey.isEmpty()) {
            logs = persistingLogger.getLogsByKeyAndSecondaryKey(productNumber, secondKey);
        } else {
            logs = persistingLogger.getLogsByKey(productNumber);
        }
        final StringBuilder logStringBuilder = new StringBuilder();
        if (logs.isEmpty()) {
            logStringBuilder.append("No log entires for ");
            logStringBuilder.append(Constants.NetLicensing.PRODUCT_NUMBER);
            logStringBuilder.append("=");
            logStringBuilder.append(productNumber);
            if (secondKey != null && !secondKey.isEmpty()) {
                logStringBuilder.append(" and ");
                logStringBuilder.append(secondKeyName);
                logStringBuilder.append("=");
                logStringBuilder.append(secondKey);
            }
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

    protected void logEntries(String productNumber, String secondKey, final List<String> licenseTemplateList,
            final MultivaluedMap<String, String> formParams, Logger logger) {
        final String logMessage =
                "Executing " + logger.getName() + " Code Generator for productNumber: " + productNumber
                        + ", licenseTemplateList: " + licenseTemplateList.toString() + ", formParams: "
                        + formParams.toString();
        persistingLogger.log(productNumber, secondKey, StoredLog.Severity.INFO, logMessage, logger);
    }

    protected void createLicenseForLicenseTemplates(Context context,
            final Map<String, LicenseTemplate> licenseTemplates, Licensee licensee) throws NetLicensingException {
        for (final LicenseTemplate licenseTemplate : licenseTemplates.values()) {
            final License newLicense = new LicenseImpl();
            newLicense.setActive(true);
            // Required for timeVolume.
            if (LicenseType.TIMEVOLUME.equals(licenseTemplate.getLicenseType())) {
                newLicense.addProperty(Constants.NetLicensing.PROP_START_DATE, "now");
            }
            LicenseService.create(context, licensee.getNumber(), licenseTemplate.getNumber(), null, newLicense);
        }
    }

    protected Licensee createLicensee(Context context, Product product, final MultivaluedMap<String, String> formParams)
            throws NetLicensingException {
        Licensee licensee = new LicenseeImpl();
        if (formParams != null) {
            EntityUtils.addCustomPropertiesToLicensee(formParams, licensee);
        }
        licensee.setActive(true);
        licensee.setProduct(product);
        licensee.addProperty(Constants.NetLicensing.PROP_MARKED_FOR_TRANSFER, "true");
        return LicenseeService.create(context, product.getNumber(), licensee);
    }

    protected static boolean isNeedCreateNewLicensee(final Licensee licensee, final String productNumber) {
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

}
