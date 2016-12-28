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
@Table(name = "CLEAN_UP")
public class CleanUp extends AbstractPersistable<String> {

    private static final long serialVersionUID = 642517696016632591L;

    public CleanUp() {
    }

    @Column(name = "TIMESTAMP", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

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
        builder.append(", timestamp=");
        builder.append(getTimestamp());
        return builder.toString();
    }

}
