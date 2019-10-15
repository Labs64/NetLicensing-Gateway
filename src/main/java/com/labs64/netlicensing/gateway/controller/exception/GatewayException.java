package com.labs64.netlicensing.gateway.controller.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public abstract class GatewayException extends WebApplicationException {

    private static final long serialVersionUID = 7794844065577092343L;

    /**
     * Construct a <code>GatewayException</code> with the specified detail message.
     *
     * @param response
     *            the detail message
     */
    public GatewayException(final Response response) {
        super(response);
    }
}
