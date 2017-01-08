package com.labs64.netlicensing.gateway.domain.repositories;

import org.springframework.data.repository.CrudRepository;

import com.labs64.netlicensing.gateway.domain.entity.TimeStamp;

/**
 * Repository to manage {@link TimeStamp} instances.
 */
public interface TimeStampRepository extends CrudRepository<TimeStamp, String> {

}
