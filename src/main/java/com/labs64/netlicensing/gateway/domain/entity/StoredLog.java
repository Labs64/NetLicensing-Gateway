package com.labs64.netlicensing.gateway.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "LOGGING")
public class StoredLog extends AbstractPersistable<String> {

    public enum Severity {
        INFO, WARNING, ERROR
    }

    public StoredLog() {
    }

    @Column(name = "KEY", nullable = true)
    private String key;

    @Column(name = "SECONDARY_KEY", nullable = true)
    private String secondaryKey;

    @Column(name = "MESSAGE", nullable = true)
    private String message;

    @Column(name = "SEVERITY", nullable = true)
    private Severity severity;

    @Column(name = "TIMESTAMP", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }

    public void setSecondaryKey(final String secondaryKey) {
        this.secondaryKey = secondaryKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(final Severity severity) {
        this.severity = severity;
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
        builder.append("key=");
        builder.append(getKey());
        builder.append(", severity=");
        builder.append(getSeverity());
        builder.append(", message=");
        builder.append(getMessage());
        builder.append(", timestamp=");
        builder.append(getTimestamp());
        return builder.toString();
    }
}
