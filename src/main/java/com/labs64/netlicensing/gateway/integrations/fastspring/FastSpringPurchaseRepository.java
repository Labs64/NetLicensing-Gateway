package com.labs64.netlicensing.gateway.integrations.fastspring;

import java.util.Date;

import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository to manage {@link FastSpringPurchase} instances.
 */
public interface FastSpringPurchaseRepository extends CrudRepository<FastSpringPurchase, String> {

    FastSpringPurchase findFirstByReferenceAndProductNumber(String reference, String productNumber);

    void deleteByTimestampBefore(@Temporal(TemporalType.TIMESTAMP) Date date);
}
