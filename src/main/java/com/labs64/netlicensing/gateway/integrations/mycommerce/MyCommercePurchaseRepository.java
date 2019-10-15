package com.labs64.netlicensing.gateway.integrations.mycommerce;

import java.util.Date;

import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository to manage {@link MyCommercePurchase} instances.
 */
public interface MyCommercePurchaseRepository extends CrudRepository<MyCommercePurchase, String> {

    MyCommercePurchase findFirstByPurchaseIdAndProductNumber(String purchaseId, String productNumber);

    void deleteByTimestampBefore(@Temporal(TemporalType.TIMESTAMP) Date date);
}
