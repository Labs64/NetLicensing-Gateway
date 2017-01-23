package com.labs64.netlicensing.gateway.bl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.print.attribute.standard.Severity;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.gateway.domain.entity.Log;
import com.labs64.netlicensing.gateway.domain.repositories.LogRepository;
import com.labs64.netlicensing.gateway.util.Constants;

@Component
public class PersistingLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistingLogger.class);

    @Inject
    private LogRepository logRepository;

    @Inject
    private TimeStampTracker timeStampTracker;

    @Transactional
    public void logRequest(final String productNumber, final List<String> licenseTemplateList,
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

    @Transactional
    public void logException(final String message, final String key) {
        final Log requestResponse = new Log();
        requestResponse.setKey(key);
        requestResponse.setSeverity(Severity.ERROR);
        requestResponse.setMessage(message);
        requestResponse.setTimestamp(new Date());
        logRepository.save(requestResponse);
        removeExpiredErrorLogs();
    }

    @Transactional
    public List<Log> getLogsByKey(final String key) {
        return logRepository.findByKey(key);
    }

    @Transactional
    private void removeExpiredErrorLogs() {
        if (timeStampTracker.isTimeOutExpired(Constants.MyCommerce.NEXT_ERROR_LOG_CLEANUP_TAG,
                Constants.MyCommerce.CLEANUP_PERIOD_MINUTES)) {
            final Calendar earliestPersistTime = Calendar.getInstance();
            earliestPersistTime.add(Calendar.DATE, -Constants.MyCommerce.PERSIST_ERROR_LOG_DAYS);
            logRepository.deleteByTimestampBefore(earliestPersistTime.getTime());
        }
    }

}
