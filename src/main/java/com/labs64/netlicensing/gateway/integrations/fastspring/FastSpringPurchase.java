package com.labs64.netlicensing.gateway.integrations.fastspring;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Represents database FastSpring licensee property entity.
 */
@Entity
@Table(name = "FAST_SPRING_PURCHASES")
public class FastSpringPurchase extends AbstractPersistable<String> {

    public FastSpringPurchase() {
    }

    @Column(name = "LICENSEE_NUMBER", nullable = false)
    private String licenseeNumber;

    @Column(name = "REFERENCE", nullable = true)
    private String reference;

    @Column(name = "PRODUCT_NUMBER", nullable = true)
    private String productNumber;

    @Column(name = "TIMESTAMP", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    public String getLicenseeNumber() {
        return licenseeNumber;
    }

    public void setLicenseeNumber(final String licenseeNumber) {
        this.licenseeNumber = licenseeNumber;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(final String productNumber) {
        this.productNumber = productNumber;
    }

    public Date getTimestamp() {
        if (timestamp == null) {
            return null;
        } else {
            return new Date(timestamp.getTime());
        }
    }

    public void setTimestamp(final Date timestamp) {
        if (timestamp == null) {
            this.timestamp = new Date();
        } else {
            this.timestamp = new Date(timestamp.getTime());
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("id=");
        builder.append(getId());
        builder.append(", licenseeNumber=");
        builder.append(getLicenseeNumber());
        builder.append(", reference=");
        builder.append(getReference());
        builder.append(", productNumber=");
        builder.append(getProductNumber());
        builder.append(", timestamp=");
        builder.append(getTimestamp());
        return builder.toString();
    }
}
