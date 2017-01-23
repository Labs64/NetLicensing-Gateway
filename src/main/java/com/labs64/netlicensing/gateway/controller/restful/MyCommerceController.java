package com.labs64.netlicensing.gateway.controller.restful;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.PersistingLogger;
import com.labs64.netlicensing.gateway.bl.mycommerce.MyCommerce;
import com.labs64.netlicensing.gateway.controller.restful.exception.MyCommerceException;
import com.labs64.netlicensing.gateway.util.Constants;

@Produces({ MediaType.TEXT_PLAIN })
@Path("/" + Constants.MyCommerce.ENDPOINT_BASE_PATH)
public class MyCommerceController extends AbstractBaseController {

    @Inject
    private MyCommerce myCommerce;

    @Inject
    private PersistingLogger persistingLogger;

    @POST
    @Path("/" + Constants.MyCommerce.ENDPOINT_PATH_CODEGEN + "/{" + Constants.MyCommerce.PRODUCT_NUMBER + "}")
    @Transactional
    public String codeGenerator(@PathParam(Constants.MyCommerce.PRODUCT_NUMBER) final String productNumber,
            @QueryParam(Constants.MyCommerce.LICENSE_TEMPLATE_NUMBER) final List<String> licenseTemplateList,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.MULTIPLE_LICENSEE) final boolean multipleLicenseeMode,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.SAVE_USER_DATA) final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) {

        final String purchaseId = formParams.getFirst(Constants.MyCommerce.PURCHASE_ID);

        if (purchaseId == null) {
            throw new MyCommerceException("'" + Constants.MyCommerce.PURCHASE_ID + "' parameter is not provided");
        }

        try {
            final Context context = getSecurityHelper().getContext();
            return myCommerce.codeGenerator(context, purchaseId, productNumber, licenseTemplateList,
                    multipleLicenseeMode,
                    isSaveUserData, formParams);
        } catch (final MyCommerceException e) {
            persistingLogger.logException(e.getMessage(), purchaseId);
            throw e;
        } catch (final NetLicensingException e) {
            persistingLogger.logException(e.getMessage(), purchaseId);
            throw new MyCommerceException(e.getMessage());
        } catch (final Exception e) {
            persistingLogger.logException(e.getMessage(), purchaseId);
            throw new MyCommerceException(e.getMessage());
        }
    }

    @GET
    @Path("/" + Constants.MyCommerce.ENDPOINT_PATH_ERROR_LOG)
    public String getErrorLog(@QueryParam(Constants.Monitoring.PURCHASE_ID) final String purchaseId) {
        return myCommerce.getErrorLog(purchaseId);
    }

}
