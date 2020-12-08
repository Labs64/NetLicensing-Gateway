package com.labs64.netlicensing.gateway.bl;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.labs64.netlicensing.gateway.domain.entity.TimeStamp;
import com.labs64.netlicensing.gateway.domain.repositories.TimeStampRepository;

@Component
public class TimeStampTracker {

    @Autowired
    private TimeStampRepository timeStampRepository;

    public boolean isTimeOutExpired(final String tsTag, final int timeOutMinutes) {
        TimeStamp nextCheckTS = timeStampRepository.findById(tsTag).orElse(null);
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
