package com.labs64.netlicensing.gateway.controller.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class MonitoringException extends GatewayException {

    private static final long serialVersionUID = 3032494846331391138L;

    public MonitoringException(final String message) {
        super(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(message).type(MediaType.TEXT_PLAIN)
                .build());
    }
}
