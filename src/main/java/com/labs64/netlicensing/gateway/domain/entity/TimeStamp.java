package com.labs64.netlicensing.gateway.domain.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents database licensee property entity.
 */
@Entity
@Table(name = "TIMESTAMPS")
public class TimeStamp implements Serializable {

    private static final long serialVersionUID = 642517696016632591L;

    public TimeStamp() {
    }

    public TimeStamp(final String id) {
        this.id = id;
    }

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
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
