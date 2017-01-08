package com.labs64.netlicensing.gateway.controller.restful;

import java.util.Calendar;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;

import com.labs64.netlicensing.gateway.domain.entity.TimeStamp;
import com.labs64.netlicensing.gateway.domain.repositories.TimeStampRepository;
import com.labs64.netlicensing.gateway.util.security.SecurityHelper;

abstract class AbstractBaseController {

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private SecurityHelper securityHelper;

    @Inject
    private TimeStampRepository timeStampRepository;

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected SecurityHelper getSecurityHelper() {
        return securityHelper;
    }

    protected boolean isTimeOutExpired(final String tsTag, final int timeOutMinutes) {
        TimeStamp nextCheckTS = timeStampRepository.findOne(tsTag);
        final Calendar now = Calendar.getInstance();
        final boolean expired = (nextCheckTS == null) || (now.getTime().after(nextCheckTS.getTimestamp()));
        if (expired) {
            if (nextCheckTS == null) {
                nextCheckTS = new TimeStamp(tsTag);
            }
            final Calendar nextCheck = Calendar.getInstance();
            nextCheck.add(Calendar.MINUTE, timeOutMinutes);
            nextCheckTS.setTimestamp(nextCheck.getTime());
            timeStampRepository.save(nextCheckTS);
        }
        return expired;
    }

}
