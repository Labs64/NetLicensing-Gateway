package com.labs64.netlicensing.gateway.controller.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.labs64.netlicensing.exception.ConversionException;

@Provider
@Produces({ MediaType.TEXT_PLAIN })
public class AppExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LogManager.getLogger(AppExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable exception) {
        LOGGER.trace(exception.getMessage(), exception);

        if (exception instanceof NotFoundException) {
            return Response.status(mapStatus(exception)).entity("Endpoint does not exist").build();
        } else if (exception instanceof WebApplicationException) {
            return Response.fromResponse(((WebApplicationException) exception).getResponse())
                    .status(mapStatus(exception)).entity("The specified method is not allowed").build();
        } else if (exception instanceof ConversionException) {
            return Response.status(mapStatus(exception))
                    .entity(exception.getClass().getSimpleName() + ": " + exception.getCause().getMessage()).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(exception.getClass().getSimpleName() + ": " + exception.getCause().getMessage()).build();
        }
    }

    /**
     * Map exception to the response code.
     *
     * @param exception
     *            exception to map
     * @return response code
     */
    private Status mapStatus(final Throwable exception) {
        final Status status = Response.Status.BAD_REQUEST;
        // TODO: map errors status
        return status;
    }
}
