package com.labs64.netlicensing.gateway.domain.repositories;

import java.util.Date;

import org.springframework.data.repository.CrudRepository;

import com.labs64.netlicensing.gateway.domain.mycommerce.entity.StoredResponse;

/**
 * Repository to manage {@link StoredResponse} instances.
 */
public interface StoredResponseRepository extends CrudRepository<StoredResponse, String> {

    StoredResponse findFirstByPurchaseId(String purchaseId);

    StoredResponse deleteByTimestampBefore(Date time);
}
