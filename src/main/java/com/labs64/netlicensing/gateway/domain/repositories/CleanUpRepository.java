package com.labs64.netlicensing.gateway.domain.repositories;

import org.springframework.data.repository.CrudRepository;

import com.labs64.netlicensing.gateway.domain.mycommerce.entity.CleanUp;

/**
 * Repository to manage {@link CleanUp} instances.
 */
public interface CleanUpRepository extends CrudRepository<CleanUp, String> {

    CleanUp findFirstByOrderByTimestampDesc();

}
