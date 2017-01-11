package com.labs64.netlicensing.gateway.controller.restful.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.schema.context.Info;
import com.labs64.netlicensing.schema.context.InfoEnum;
import com.labs64.netlicensing.schema.context.Netlicensing;
import com.labs64.netlicensing.schema.context.ObjectFactory;

public class MyCommerceException extends GatewayException {

    private static final long serialVersionUID = 3575586728122361138L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MyCommerceException.class);

    public MyCommerceException(final String message) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(buildMessage(message)).type(MediaType.APPLICATION_XML)
                .build());
    }

    private static Netlicensing buildMessage(final String message) {
        LOGGER.error(message);

        final ObjectFactory objectFactory = new ObjectFactory();
        final Netlicensing resp = objectFactory.createNetlicensing();
        resp.setInfos(objectFactory.createNetlicensingInfos());

        resp.getInfos().getInfo()
        .add(new Info(message, String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()), InfoEnum.ERROR));
        return resp;
    }

}
