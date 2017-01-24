package com.labs64.netlicensing.gateway.controller.restful;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.controller.restful.exception.MonitoringException;
import com.labs64.netlicensing.gateway.domain.repositories.TimeStampRepository;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.UtilityService;;

@Component
@Path("/" + Constants.Monitoring.ENDPOINT_BASE_PATH)
public class MonitoringController extends AbstractBaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringController.class);

    @Inject
    private TimeStampRepository timeStampRepository;

    @Value("${project.name}")
    private String projectName;

    @Value("${project.version}")
    private String projectVersion;

    @GET
    @Path("/")
    public String monitoring() throws NetLicensingException {
        checkNetLicensingAvailability();
        checkDatabaseAvailability();
        return projectName + " " + projectVersion + " is up and running.";
    }

    private void checkNetLicensingAvailability() {
        try {
            final Context context = getSecurityHelper().getDemoContext();
            UtilityService.listLicenseTypes(context); // Dummy operation
        } catch (final Exception e) {
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