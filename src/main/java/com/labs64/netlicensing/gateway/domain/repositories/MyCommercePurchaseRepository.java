package com.labs64.netlicensing.gateway.domain.repositories;

import java.util.Date;

import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;

import com.labs64.netlicensing.gateway.domain.entity.MyCommercePurchase;

/**
 * Repository to manage {@link MyCommercePurchase} instances.
 */
public interface MyCommercePurchaseRepository extends CrudRepository<MyCommercePurchase, String> {

    MyCommercePurchase findFirstByPurchaseId(String purchaseId);

    void deleteByTimestampBefore(@Temporal(TemporalType.TIMESTAMP) Date date);
}
