package com.labs64.netlicensing.gateway.domain.mycommerce.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Represents database licensee property entity.
 */
@Entity
@Table(name = "MY_COMMERCE")
public class StoredResponse extends AbstractPersistable<String> {

    private static final long serialVersionUID = 6805624114204054433L;

    public StoredResponse() {
    }

    // public StoredResponse(final String id) {
    // setId(id);
    // }

    @Column(name = "LICENSEE_NUMBER", nullable = false)
    private String licenseeNumber;

    @Column(name = "PURCHASE_ID", nullable = true)
    private String purchaseId;

    @Column(name = "TIMESTAMP", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    public String getLicenseeNumber() {
        return licenseeNumber;
    }

    public void setLicenseeNumber(final String licenseeNumber) {
        this.licenseeNumber = licenseeNumber;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(final String purchaseId) {
        this.purchaseId = purchaseId;
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
        builder.append(", purchaseId=");
        builder.append(getPurchaseId());
        builder.append(", timestamp=");
        builder.append(getTimestamp());
        return builder.toString();
    }

}
