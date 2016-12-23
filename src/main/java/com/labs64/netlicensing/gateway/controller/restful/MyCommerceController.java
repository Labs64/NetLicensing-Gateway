package com.labs64.netlicensing.gateway.controller.restful;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

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
@Path("/myCommerce")
public class MyCommerceController extends AbstractBaseController {

    @POST
    @Path("/" + Constants.myCommerce.ENDPOINT_PATH_KEYGEN + "/{" + Constants.myCommerce.LICENSE_TEMPLATE + "}")
    public String keygen(@PathParam(Constants.myCommerce.LICENSE_TEMPLATE) final String licenseTemplate,
            final MultivaluedMap<String, String> formParams)
                    throws NetLicensingException, UnsupportedEncodingException {
        final Context context = getSecurityHelper().getContext();

        if ((formParams != null)) {
            final String product_id = formParams.getFirst(Constants.myCommerce.PRODUCT_ID);
            final String lastname = (formParams.getFirst(Constants.myCommerce.LASTNAME) != null
                    ? formParams.getFirst(Constants.myCommerce.LASTNAME) : "");
            final String firstname = (formParams.getFirst(Constants.myCommerce.FIRSTNAME) != null
                    ? formParams.getFirst(Constants.myCommerce.FIRSTNAME) : "");
            final String email = (formParams.getFirst(Constants.myCommerce.EMAIL) != null
                    ? formParams.getFirst(Constants.myCommerce.EMAIL) : "");

            final Product product = ProductService.get(context, product_id);
            if (product != null) {
                final Licensee newLicensee = new LicenseeImpl();
                newLicensee.setActive(true);
                newLicensee.setProduct(product);
                newLicensee.addProperty(Constants.myCommerce.EMAIL, email);
                newLicensee.addProperty(Constants.myCommerce.LASTNAME, lastname);
                newLicensee.addProperty(Constants.myCommerce.FIRSTNAME, firstname);

                final Licensee createdLicensee = LicenseeService.create(context, product_id, newLicensee);

                if (createdLicensee.getNumber() != null) {
                    final License newLicense = new LicenseImpl();
                    newLicense.setActive(true);

                    final License createdLicense = LicenseService.create(context, createdLicensee.getNumber(),
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
}
