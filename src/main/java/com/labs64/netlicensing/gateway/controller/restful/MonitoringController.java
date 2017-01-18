package com.labs64.netlicensing.gateway.controller.restful;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.controller.restful.exception.MonitoringException;
import com.labs64.netlicensing.gateway.domain.repositories.MyCommercePurchaseRepository;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.UtilityService;;

@Path("/" + Constants.Monitoring.ENDPOINT_BASE_PATH)
public class MonitoringController extends AbstractBaseController {

    @Inject
    private MyCommercePurchaseRepository myCommercePurchaseRepository;

    @GET
    @Path("/")
    public String monitoring() throws NetLicensingException {
        checkNetLicensingAvailability();
        checkDatabaseAvailability();
        return "Gateway is work";
    }

    private void checkNetLicensingAvailability() {
        final Context context = getSecurityHelper().getContext();
        try {
            UtilityService.listLicenseTypes(context);
        } catch (final NetLicensingException e) {
            throw new MonitoringException("NetLicensing temporarily unavailable");
        }
    }

    private void checkDatabaseAvailability() {
        try {
            myCommercePurchaseRepository.count();
        } catch (final Exception ex) {
            throw new MonitoringException("Database unavailable");
        }
    }
}