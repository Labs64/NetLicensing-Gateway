package com.labs64.netlicensing.gateway.domain.repositories;

import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;

import com.labs64.netlicensing.gateway.domain.entity.Log;

/**
 * Repository to manage {@link Log} instances.
 */
public interface LogRepository extends CrudRepository<Log, String> {

    List<Log> findByKey(String key);

    void deleteByTimestampBefore(@Temporal(TemporalType.TIMESTAMP) Date date);

}
