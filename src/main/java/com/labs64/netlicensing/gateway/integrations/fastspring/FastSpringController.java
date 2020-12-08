package com.labs64.netlicensing.gateway.integrations.fastspring;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.PersistingLogger;
import com.labs64.netlicensing.gateway.controller.restful.AbstractBaseController;
import com.labs64.netlicensing.gateway.domain.entity.StoredLog;
import com.labs64.netlicensing.gateway.integrations.common.BaseException;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.gateway.util.security.SecurityHelper;

@Component
@Produces({ MediaType.TEXT_PLAIN })
@Path("/" + FastSpring.FastSpringConstants.ENDPOINT_BASE_PATH)
public class FastSpringController extends AbstractBaseController {

    @Autowired
    private FastSpring fastSpring;

    @Autowired
    private PersistingLogger persistingLogger;

    @POST
    @Path("/" + Constants.ENDPOINT_PATH_CODEGEN)
    @Transactional
    public String codeGenerator(final MultivaluedMap<String, String> formParams) throws NetLicensingException {

        final Context context = getSecurityHelper().getContext();

        final String apiKey = formParams.getFirst(FastSpring.FastSpringConstants.API_KEY);
        if (apiKey.isEmpty()) {
            throw new BaseException("'" + FastSpring.FastSpringConstants.API_KEY + "' parameter is required");
        }
        context.setSecurityMode(SecurityMode.APIKEY_IDENTIFICATION);
        context.setApiKey(apiKey);

        if (!fastSpring.isPrivateKeyValid(context, formParams)) {
            throw new BaseException("Property '" + FastSpring.FastSpringConstants.PRIVATE_KEY + "' do not match");
        }

        if (!SecurityHelper.checkContextConnection(context)) {
            throw new BaseException("Wrong " + FastSpring.FastSpringConstants.API_KEY + " provided");
        }

        final String reference = formParams.getFirst(FastSpring.FastSpringConstants.REFERENCE);
        final String productNumber = formParams.getFirst(Constants.NetLicensing.PRODUCT_NUMBER);

        final List<String> licenseTemplateList = Arrays.asList(
                formParams.getFirst(FastSpring.FastSpringConstants.LICENSE_TEMPLATE_LIST).split("\\s*,\\s*"));

        if (StringUtils.isEmpty(productNumber) || licenseTemplateList.isEmpty()) {
            throw new BaseException("Required parameters not provided");
        }

        if (StringUtils.isEmpty(reference)) {
            final String message = "'" + FastSpring.FastSpringConstants.REFERENCE + "' is not provided";
            persistingLogger.log(productNumber, null, StoredLog.Severity.ERROR, message);
            throw new BaseException(message);
        }

        //default false
        final boolean isSaveUserData = Boolean.parseBoolean(formParams.getFirst(Constants.SAVE_USER_DATA));
        //default true
        String quantityToLicenseeParam = formParams.getFirst(Constants.QUANTITY_TO_LICENSEE);
        boolean quantityToLicensee = quantityToLicenseeParam == null || Boolean.parseBoolean(quantityToLicenseeParam);

        try {
            return fastSpring.codeGenerator(context, reference, productNumber, licenseTemplateList, quantityToLicensee,
                    isSaveUserData, formParams);
        } catch (final BaseException e) {
            persistingLogger.log(productNumber, reference, StoredLog.Severity.ERROR,
                    e.getResponse().getEntity().toString());
            throw e;
        } catch (final Exception e) {
            persistingLogger.log(productNumber, reference, StoredLog.Severity.ERROR, e.getMessage());
            throw new BaseException(e.getMessage());
        }
    }

    @GET
    @Path("/" + Constants.ENDPOINT_PATH_LOG + "/{" + Constants.NetLicensing.PRODUCT_NUMBER + "}")
    public String getErrorLog(@PathParam(Constants.NetLicensing.PRODUCT_NUMBER) final String productNumber,
            @QueryParam(FastSpring.FastSpringConstants.REFERENCE) final String reference) {
        try {
            final Context context = getSecurityHelper().getContext();
            return fastSpring.getErrorLog(context, productNumber, reference, FastSpring.FastSpringConstants.REFERENCE);
        } catch (final Exception e) {
            throw new BaseException(e.getMessage());
        }
    }
}
