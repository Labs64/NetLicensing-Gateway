package com.labs64.netlicensing.gateway.bl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.gateway.domain.entity.StoredLog;
import com.labs64.netlicensing.gateway.domain.repositories.LogRepository;
import com.labs64.netlicensing.gateway.util.Constants;

// TODO(2K): turn into sink for slf4j

@Component
public class PersistingLogger {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private TimeStampTracker timeStampTracker;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void log(final String key, final String secondaryKey, final StoredLog.Severity severity, final String msg) {
        log(key, secondaryKey, severity, msg, null);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void log(final String key, final String secondaryKey, final StoredLog.Severity severity, final String msg,
            final Logger logger) {
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
        requestResponse.setSecondaryKey(secondaryKey);
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

    @Transactional
    public List<StoredLog> getLogsByKeyAndSecondaryKey(final String key, final String secondaryKey) {
        return logRepository.findByKeyAndSecondaryKey(key, secondaryKey);
    }
}
