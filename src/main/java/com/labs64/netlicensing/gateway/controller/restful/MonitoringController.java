package com.labs64.netlicensing.gateway.controller.restful;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.Page;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.controller.restful.exception.MonitoringException;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.UtilityService;;

@Path("/" + Constants.Monitoring.ENDPOINT_BASE_PATH)
public class MonitoringController extends AbstractBaseController {

    @GET
    @Path("/")
    public String monitoring() throws NetLicensingException {
        final Context context = getSecurityHelper().getContext();
        final Page<String> licenseTypes = UtilityService.listLicenseTypes(context);
        if (licenseTypes == null) {
            throw new MonitoringException("NetLicensing temporarily unavailable");
        }

        return "Gateway is work";
    }
}