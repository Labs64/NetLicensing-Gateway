package com.labs64.netlicensing.gateway.controller.restful;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.gateway.controller.exception.MonitoringException;
import com.labs64.netlicensing.gateway.domain.repositories.TimeStampRepository;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.UtilityService;

@Component
@Path("/" + Constants.Monitoring.ENDPOINT_BASE_PATH)
public class MonitoringController extends AbstractBaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringController.class);

    @Autowired
    private TimeStampRepository timeStampRepository;

    @Value("${project.name}")
    private String projectName;

    @Value("${project.version}")
    private String projectVersion;

    @GET
    @Path("/")
    public String monitoring() {
        checkNetLicensingAvailability();
        checkDatabaseAvailability();
        return projectName + " v" + projectVersion + " is up and running.";
    }

    private void checkNetLicensingAvailability() {
        try {
            final Context context = getSecurityHelper().getMonitoringContext();
            UtilityService.listLicenseTypes(context);
        } catch (final Exception e) {
            LOGGER.error("Monitoring: NetLicensing Error: " + e.getMessage());
            throw new MonitoringException("NetLicensing is not reachable.");
        }
    }

    private void checkDatabaseAvailability() {
        try {
            timeStampRepository.count();
        } catch (final Exception e) {
            LOGGER.error("Monitoring: Database Error: " + e.getMessage());
            throw new MonitoringException("Database unavailable");
        }
    }
}
