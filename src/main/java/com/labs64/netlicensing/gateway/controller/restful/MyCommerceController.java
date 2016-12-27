package com.labs64.netlicensing.gateway.controller.restful;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;

import com.labs64.netlicensing.domain.entity.License;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.impl.LicenseImpl;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseService;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;

@Produces({ MediaType.TEXT_PLAIN })
@Path("/mycommerce")
public class MyCommerceController extends AbstractBaseController {

    @POST
    @Path("/" + Constants.myCommerce.ENDPOINT_PATH_KEYGEN + "/{" + Constants.myCommerce.PRODUCT_NUMBER + "}" + "/{"
            + Constants.myCommerce.LICENSE_TEMPLATE + "}")
    public String keygen(@PathParam(Constants.myCommerce.PRODUCT_NUMBER) final String productNumber,
            @PathParam(Constants.myCommerce.LICENSE_TEMPLATE) final String licenseTemplate,
            @DefaultValue("false") @QueryParam("saveUserData") final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams)
                    throws NetLicensingException, UnsupportedEncodingException, InterruptedException {
        final Context context = getSecurityHelper().getContext();


        if ((formParams != null)) {
            Licensee licensee = new LicenseeImpl();
            final Product product = ProductService.get(context, productNumber);
            if (product != null) {

                // get existing Licensee or create new
                final String licenseeNumber = formParams.getFirst(Constants.myCommerce.LICENSEE_NUMBER);
                if (StringUtils.isNotBlank(licenseeNumber)) {
                    licensee = LicenseeService.get(context, licenseeNumber);
                    if (licensee == null) {
                        throw new BadRequestException("Incorrect Licensee number");
                    }
                } else {
                    if (isSaveUserData) {
                        licensee = addCustomPropertyToLicensee(formParams, licensee);
                    }
                    licensee.setActive(true);
                    licensee.setProduct(product);

                    licensee = LicenseeService.create(context, productNumber, licensee);
                }

                if (licensee.getNumber() != null) {
                    final License newLicense = new LicenseImpl();
                    newLicense.setActive(true);
                    final License createdLicense = LicenseService.create(context, licensee.getNumber(),
                            licenseTemplate, null, newLicense);
                    return createdLicense.getNumber();
                } else {
                    throw new BadRequestException("Incorrect Licensee");
                }
            } else {
                throw new BadRequestException("Incorrect Product");
            }
        } else {
            throw new BadRequestException("Incorrect data");
        }
    }

    private Licensee addCustomPropertyToLicensee(final MultivaluedMap<String, String> formParams,
            final Licensee licensee) {
        // Custom properties
        for (final Map.Entry<String, List<String>> entry : formParams.entrySet()) {
            if (!LicenseeImpl.getReservedProps().contains(entry.getKey()) && !entry.getValue().get(0).equals("")) {
                licensee.addProperty(entry.getKey(), entry.getValue().get(0));
            }
        }
        return licensee;
    }
}
