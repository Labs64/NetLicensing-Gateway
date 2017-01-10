package com.labs64.netlicensing.gateway.controller.restful.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.labs64.netlicensing.exception.ConversionException;
import com.labs64.netlicensing.schema.context.Info;
import com.labs64.netlicensing.schema.context.InfoEnum;
import com.labs64.netlicensing.schema.context.Netlicensing;
import com.labs64.netlicensing.schema.context.ObjectFactory;

@Provider
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class AppExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(final Throwable exception) {
        final ObjectFactory objectFactory = new ObjectFactory();
        final Netlicensing resp = objectFactory.createNetlicensing();
        resp.setInfos(objectFactory.createNetlicensingInfos());

        if (exception instanceof NotFoundException) {
            resp.getInfos().getInfo().add(new Info("Endpoint does not exist",
                    String.valueOf(Response.Status.NOT_FOUND.getStatusCode()), InfoEnum.ERROR));
            return Response.status(mapStatus(exception)).entity(resp).build();
        } else if (exception instanceof WebApplicationException) {
            resp.getInfos().getInfo().add(new Info("The specified method is not allowed",
                    String.valueOf(Response.Status.NOT_FOUND.getStatusCode()), InfoEnum.ERROR));
            return Response.fromResponse(((javax.ws.rs.WebApplicationException) exception).getResponse())
                    .status(mapStatus(exception)).entity(resp).build();
        } else if (exception instanceof ConversionException) {
            resp.getInfos().getInfo().add(
                    new Info(exception.getCause().getMessage(), exception.getClass().getSimpleName(), InfoEnum.ERROR));
            return Response.status(mapStatus(exception)).entity(resp).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(resp).build();
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
        // TODO
        return status;
    }
}
