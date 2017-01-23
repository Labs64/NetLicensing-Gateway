package com.labs64.netlicensing.gateway.bl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.gateway.domain.entity.StoredLog;
import com.labs64.netlicensing.gateway.domain.repositories.LogRepository;
import com.labs64.netlicensing.gateway.util.Constants;

// TODO(2K): turn into sink for slf4j

@Component
public class PersistingLogger {

    @Inject
    private LogRepository logRepository;

    @Inject
    private TimeStampTracker timeStampTracker;

    public void log(final String key, final StoredLog.Severity severity, final String msg) {
        log(key, severity, msg, null);
    }

    @Transactional
    public void log(final String key, final StoredLog.Severity severity, final String msg, final Logger logger) {
        if (logger != null) {
            switch (severity) {
            case ERROR:
                logger.error(msg);
                break;
            case WARNING:
                logger.warn(msg);
                break;
            case INFO:
                logger.info(msg);
                break;
            }
        }
        final StoredLog requestResponse = new StoredLog();
        requestResponse.setKey(key);
        requestResponse.setSeverity(severity);
        requestResponse.setMessage(msg);
        requestResponse.setTimestamp(new Date());
        logRepository.save(requestResponse);

        if (timeStampTracker.isTimeOutExpired(Constants.LOG_NEXT_CLEANUP_TAG, Constants.CLEANUP_PERIOD_MINUTES)) {
            final Calendar earliestPersistTime = Calendar.getInstance();
            earliestPersistTime.add(Calendar.DAY_OF_MONTH, -Constants.LOG_PERSIST_DAYS);
            logRepository.deleteByTimestampBefore(earliestPersistTime.getTime());
        }
    }

    @Transactional
    public List<StoredLog> getLogsByKey(final String key) {
        return logRepository.findByKey(key);
    }

}