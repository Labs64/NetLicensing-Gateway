package com.labs64.netlicensing.gateway.domain.repositories;

import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;

import com.labs64.netlicensing.gateway.domain.entity.StoredLog;

/**
 * Repository to manage {@link StoredLog} instances.
 */
public interface LogRepository extends CrudRepository<StoredLog, String> {

    List<StoredLog> findByKey(String key);

    void deleteByTimestampBefore(@Temporal(TemporalType.TIMESTAMP) Date date);

}
