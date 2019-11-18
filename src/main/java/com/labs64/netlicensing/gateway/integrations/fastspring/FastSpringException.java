package com.labs64.netlicensing.gateway.integrations.fastspring;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.gateway.controller.exception.GatewayException;

public class FastSpringException extends GatewayException {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastSpringException.class);

    public FastSpringException(final String message) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN).build());
        LOGGER.error(message);
    }
}
