package com.labs64.netlicensing.gateway.controller.restful;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.controller.restful.exception.MonitoringException;
import com.labs64.netlicensing.gateway.domain.repositories.TimeStampRepository;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.UtilityService;;

@Path("/" + Constants.Monitoring.ENDPOINT_BASE_PATH)
public class MonitoringController extends AbstractBaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringController.class);

    @Inject
    private TimeStampRepository timeStampRepository;

    @GET
    @Path("/")
    public String monitoring() throws NetLicensingException {
        checkNetLicensingAvailability();
        checkDatabaseAvailability();
        return "NetLicensing Gateway is up and running.";
    }

    private void checkNetLicensingAvailability() {
        final Context context = getSecurityHelper().getContext();
        try {
            UtilityService.listLicenseTypes(context); // Dummy operation
        } catch (final NetLicensingException e) {
            LOGGER.error("Monitoring, netlicensing error: " + e.getMessage());
            throw new MonitoringException("NetLicensing is not reachable.");
        }
    }

    private void checkDatabaseAvailability() {
        try {
            timeStampRepository.count(); // Dummy operation
        } catch (final Exception e) {
            LOGGER.error("Monitoring, database error: " + e.getMessage());
            throw new MonitoringException("Database unavailable");
        }
    }
}