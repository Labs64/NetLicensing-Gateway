package com.labs64.netlicensing.gateway.integrations.mycommerce;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.gateway.bl.PersistingLogger;
import com.labs64.netlicensing.gateway.controller.restful.AbstractBaseController;
import com.labs64.netlicensing.gateway.domain.entity.StoredLog;
import com.labs64.netlicensing.gateway.integrations.common.BaseException;
import com.labs64.netlicensing.gateway.util.Constants;

@Component
@Produces({ MediaType.TEXT_PLAIN })
@Path("/" + MyCommerce.MyCommerceConstants.ENDPOINT_BASE_PATH)
public class MyCommerceController extends AbstractBaseController {

    @Autowired
    private MyCommerce myCommerce;

    @Autowired
    private PersistingLogger persistingLogger;

    @POST
    @Path("/" + Constants.ENDPOINT_PATH_CODEGEN + "/{" + Constants.NetLicensing.PRODUCT_NUMBER + "}")
    @Transactional
    public String codeGenerator(@PathParam(Constants.NetLicensing.PRODUCT_NUMBER) final String productNumber,
            @QueryParam(Constants.NetLicensing.LICENSE_TEMPLATE_NUMBER) final List<String> licenseTemplateList,
            @DefaultValue("true") @QueryParam(Constants.QUANTITY_TO_LICENSEE) final boolean quantityToLicensee,
            @DefaultValue("false") @QueryParam(Constants.SAVE_USER_DATA) final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) {

        final String purchaseId = formParams.getFirst(MyCommerce.MyCommerceConstants.PURCHASE_ID);

        if (purchaseId == null) {
            final String message = "'" + MyCommerce.MyCommerceConstants.PURCHASE_ID + "' is not provided";
            persistingLogger.log(productNumber, null, StoredLog.Severity.ERROR, message);
            throw new BaseException(message);
        }

        try {
            final Context context = getSecurityHelper().getContext();
            return myCommerce.codeGenerator(context, purchaseId, productNumber, licenseTemplateList, quantityToLicensee,
                    isSaveUserData, formParams);
        } catch (final BaseException e) {
            persistingLogger.log(productNumber, purchaseId, StoredLog.Severity.ERROR,
                    e.getResponse().getEntity().toString());
            throw e;
        } catch (final Exception e) {
            persistingLogger.log(productNumber, purchaseId, StoredLog.Severity.ERROR, e.getMessage());
            throw new BaseException(e.getMessage());
        }
    }

    @GET
    @Path("/" + Constants.ENDPOINT_PATH_LOG + "/{" + Constants.NetLicensing.PRODUCT_NUMBER + "}")
    public String getErrorLog(@PathParam(Constants.NetLicensing.PRODUCT_NUMBER) final String productNumber,
            @QueryParam(MyCommerce.MyCommerceConstants.PURCHASE_ID) final String purchaseId) {
        try {
            final Context context = getSecurityHelper().getContext();
            return myCommerce.getErrorLog(context, productNumber, purchaseId,
                    MyCommerce.MyCommerceConstants.PURCHASE_ID);
        } catch (final Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

}
