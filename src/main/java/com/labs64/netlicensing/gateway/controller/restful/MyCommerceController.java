package com.labs64.netlicensing.gateway.controller.restful;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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
import com.labs64.netlicensing.gateway.domain.mycommerce.entity.CleanUp;
import com.labs64.netlicensing.gateway.domain.mycommerce.entity.StoredResponse;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseService;
import com.labs64.netlicensing.service.LicenseTemplateService;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;

@Produces({ MediaType.TEXT_PLAIN })
@Path("/mycommerce")
public class MyCommerceController extends AbstractBaseController {

    @POST
    @Path("/" + Constants.myCommerce.ENDPOINT_PATH_KEYGEN + "/{" + Constants.myCommerce.PRODUCT_NUMBER + "}")
    public String keygen(@PathParam(Constants.myCommerce.PRODUCT_NUMBER) final String productNumber,
            @QueryParam("licenseTemplateNumber") final List<String> licenseTemplateList,
            @DefaultValue("false") @QueryParam("saveUserData") final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams)
                    throws UnsupportedEncodingException, InterruptedException {
        final Context context = getSecurityHelper().getContext();

        if (!formParams.isEmpty() && !licenseTemplateList.isEmpty()) {
            Product product;
            try {
                // get product
                product = ProductService.get(context, productNumber);

                // check licenseTemplate
                checkLicenseTemplate(context, licenseTemplateList);

                // get existing Licensee or create new
                Licensee licensee = new LicenseeImpl();
                final String licenseeNumber = formParams.getFirst(Constants.myCommerce.LICENSEE_NUMBER);
                final String purchaseId = formParams.getFirst(Constants.myCommerce.PURCHASE_ID);
                licensee = getExistingLicensee(context, licenseeNumber, purchaseId);

                // create new Licensee
                if (StringUtils.isBlank(licensee.getNumber())) {
                    if (isSaveUserData) {
                        licensee = addCustomPropertyToLicensee(formParams, licensee);
                    }
                    licensee.setActive(true);
                    licensee.setProduct(product);
                    licensee = LicenseeService.create(context, productNumber, licensee);
                }

                if (licensee.getNumber() != null) {
                    // create license
                    final Iterator<String> licenseTemplateIterator = licenseTemplateList.iterator();
                    while (licenseTemplateIterator.hasNext()) {
                        final License newLicense = new LicenseImpl();
                        newLicense.setActive(true);
                        LicenseService.create(context, licensee.getNumber(), licenseTemplateIterator.next(), null,
                                newLicense);
                    }
                    // save licensee number in database
                    saveLicenseeToDatabase(licensee.getNumber(), purchaseId);

                    // check last clean up and clean if need
                    checkLastCleanUpAndClean();

                    return licensee.getNumber();
                } else {
                    throw new BadRequestException("Incorrect Licensee");
                }
            } catch (final NetLicensingException e) {
                throw new BadRequestException("Incorrect data");
            }
        } else {
            throw new BadRequestException("Incorrect data");
        }
    }

    private void saveLicenseeToDatabase(final String licenseeNumber, final String purchaseId) {
        // save licensee number in database
        final StoredResponse storedResponse = new StoredResponse();
        storedResponse.setLicenseeNumber(licenseeNumber);
        storedResponse.setPurchaseId(purchaseId);
        storedResponse.setTimestamp(new Date());
        getStoredResponseRepository().save(storedResponse);
    }

    private void checkLastCleanUpAndClean() {
        // get last clean up
        final CleanUp cleanUp = getCleanUpRepository().findFirstByOrderByTimestampDesc();
        if (cleanUp != null) {
            final int diffInHours = (int) (((new Date()).getTime() - cleanUp.getTimestamp().getTime())
                    / (1000 * 60 * 60));

            // clean
            if (diffInHours > 1) {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_MONTH, -3);
                cal.getTime();
                getStoredResponseRepository().deleteByTimestampBefore(cal.getTime());

                // save last clean up
                cleanUp.setTimestamp(new Date());
                getCleanUpRepository().save(cleanUp);
            }
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

    private void checkLicenseTemplate(final Context context, final List<String> licenseTemplateList) {
        // check licenseTemplate
        final Iterator<String> licenseTemplateIterator = licenseTemplateList.iterator();
        while (licenseTemplateIterator.hasNext()) {
            try {
                LicenseTemplateService.get(context, licenseTemplateIterator.next());
            } catch (final NetLicensingException e) {
                throw new BadRequestException("Incorrect License Template");
            }
        }
    }

    private Licensee getExistingLicensee(final Context context, final String licenseeNumber, final String purchaseId) {
        StoredResponse storedResponse = new StoredResponse();
        Licensee licensee = new LicenseeImpl();

        // if LICENSEE_NUMBER from additional field
        if (StringUtils.isNotBlank(licenseeNumber)) {
            try {
                licensee = LicenseeService.get(context, licenseeNumber);
            } catch (final NetLicensingException e) {
                throw new BadRequestException("Incorrect Licensee number");
            }

            // get from database
        } else if (StringUtils.isNotBlank(purchaseId)) {
            storedResponse = getStoredResponseRepository().findFirstByPurchaseId(purchaseId);
            if (storedResponse != null) {
                try {
                    licensee = LicenseeService.get(context, storedResponse.getLicenseeNumber());
                } catch (final NetLicensingException e) {
                    throw new BadRequestException("Incorrect Licensee number");
                }
            }
        }
        return licensee;
    }
}
