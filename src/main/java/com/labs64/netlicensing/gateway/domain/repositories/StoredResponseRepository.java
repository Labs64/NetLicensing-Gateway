package com.labs64.netlicensing.gateway.domain.repositories;

import java.util.Date;

import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;

import com.labs64.netlicensing.gateway.domain.mycommerce.entity.StoredResponse;

/**
 * Repository to manage {@link StoredResponse} instances.
 */
public interface StoredResponseRepository extends CrudRepository<StoredResponse, String> {

    StoredResponse findFirstByPurchaseId(String purchaseId);

    void deleteByTimestampBefore(@Temporal(TemporalType.TIMESTAMP) Date date);
}
