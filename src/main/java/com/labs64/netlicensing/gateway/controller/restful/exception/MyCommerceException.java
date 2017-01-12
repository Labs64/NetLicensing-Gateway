package com.labs64.netlicensing.gateway.controller.restful.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCommerceException extends GatewayException {

    private static final long serialVersionUID = 3575586728122361138L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MyCommerceException.class);

    public MyCommerceException(final String message) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN)
                .build());
        LOGGER.error(message);
    }
}
