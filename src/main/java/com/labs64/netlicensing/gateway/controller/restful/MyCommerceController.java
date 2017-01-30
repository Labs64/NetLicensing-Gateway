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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.PersistingLogger;
import com.labs64.netlicensing.gateway.bl.mycommerce.MyCommerce;
import com.labs64.netlicensing.gateway.controller.restful.exception.MyCommerceException;
import com.labs64.netlicensing.gateway.domain.entity.StoredLog;
import com.labs64.netlicensing.gateway.util.Constants;

@Component
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
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.QUANTITY_TO_LICENSEE) final boolean quantityToLicensee,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.SAVE_USER_DATA) final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) {

        final String purchaseId = formParams.getFirst(Constants.MyCommerce.PURCHASE_ID);

        if (purchaseId == null) {
            final String message = "'" + Constants.MyCommerce.PURCHASE_ID + "' is not provided";
            persistingLogger.log(productNumber, null, StoredLog.Severity.ERROR, message);
            throw new MyCommerceException(message);
        }

        try {
            final Context context = getSecurityHelper().getContext();
            return myCommerce.codeGenerator(context, purchaseId, productNumber, licenseTemplateList,
                    quantityToLicensee, isSaveUserData, formParams);
        } catch (final MyCommerceException e) {
            persistingLogger.log(productNumber, purchaseId, StoredLog.Severity.ERROR,
                    e.getResponse().getEntity().toString());
            throw e;
        } catch (final NetLicensingException e) {
            persistingLogger.log(productNumber, purchaseId, StoredLog.Severity.ERROR, e.getMessage());
            throw new MyCommerceException(e.getMessage());
        } catch (final Exception e) {
            persistingLogger.log(productNumber, purchaseId, StoredLog.Severity.ERROR, e.getMessage());
            throw new MyCommerceException(e.getMessage());
        }
    }

    @GET
    @Path("/" + Constants.MyCommerce.ENDPOINT_PATH_LOG)
    public String getErrorLog(@QueryParam(Constants.MyCommerce.PRODUCT_ID) final String productNumber,
            @QueryParam(Constants.MyCommerce.PURCHASE_ID) final String purchaseId) {
        if (productNumber == null) {
            throw new MyCommerceException("'" + Constants.MyCommerce.PRODUCT_ID + "' parameter is not provided");
        }
        try {
            final Context context = getSecurityHelper().getContext();
            return myCommerce.getErrorLog(context, productNumber, purchaseId);
        } catch (final NetLicensingException e) {
            persistingLogger.log(productNumber, purchaseId, StoredLog.Severity.ERROR, e.getMessage());
            throw new MyCommerceException(e.getMessage());
        } catch (final Exception e) {
            persistingLogger.log(productNumber, purchaseId, StoredLog.Severity.ERROR, e.getMessage());
            throw new MyCommerceException(e.getMessage());
        }
    }

}
