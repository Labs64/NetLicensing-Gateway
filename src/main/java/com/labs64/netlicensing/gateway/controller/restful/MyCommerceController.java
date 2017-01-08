package com.labs64.netlicensing.gateway.controller.restful;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
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
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.domain.entity.License;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.impl.LicenseImpl;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.domain.entity.MyCommercePurchase;
import com.labs64.netlicensing.gateway.domain.repositories.MyCommercePurchaseRepository;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseService;
import com.labs64.netlicensing.service.LicenseTemplateService;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;

@Produces({ MediaType.TEXT_PLAIN })
@Path("/" + Constants.MyCommerce.ENDPOINT_BASE_PATH)
public class MyCommerceController extends AbstractBaseController {

    @Inject
    private MyCommercePurchaseRepository myCommercePurchaseRepository;

    @POST
    @Path("/" + Constants.MyCommerce.ENDPOINT_PATH_CODEGEN + "/{" + Constants.MyCommerce.PRODUCT_NUMBER + "}")
    @Transactional
    public String keygen(@PathParam(Constants.MyCommerce.PRODUCT_NUMBER) final String productNumber,
            @QueryParam(Constants.MyCommerce.LICENSE_TEMPLATE_NUMBER) final List<String> licenseTemplateList,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.SAVE_USER_DATA) final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams)
                    throws UnsupportedEncodingException, InterruptedException {
        final Context context = getSecurityHelper().getContext();

        if (formParams.isEmpty() || licenseTemplateList.isEmpty()) {
            // TODO(2K): more detailed check (e.g. what if 'ADD[LICENSEENUMBER]' is passed, but not 'PURCHASE_ID'?
            throw new BadRequestException("Required parameters not provided");
        }

        Product product;
        try {
            product = ProductService.get(context, productNumber);
            checkLicenseTemplates(context, licenseTemplateList);

            // try to get existing Licensee
            final String licenseeNumber = formParams.getFirst(Constants.MyCommerce.LICENSEE_NUMBER);
            final String purchaseId = formParams.getFirst(Constants.MyCommerce.PURCHASE_ID);
            Licensee licensee = getExistingLicensee(context, licenseeNumber, purchaseId);

            // create new Licensee, if not existing
            if (licensee == null) {
                licensee = new LicenseeImpl();
                if (isSaveUserData) {
                    addCustomPropertiesToLicensee(formParams, licensee);
                }
                licensee.setActive(true);
                licensee.setProduct(product);
                licensee = LicenseeService.create(context, productNumber, licensee);
            }

            // create licenses
            final Iterator<String> licenseTemplateIterator = licenseTemplateList.iterator();
            while (licenseTemplateIterator.hasNext()) {
                final License newLicense = new LicenseImpl();
                newLicense.setActive(true);
                // Required for timeVolume, no harm for other types. TODO(2K): remove once inconsistency resolved.
                newLicense.addProperty(Constants.PROP_START_DATE, "now");
                LicenseService.create(context, licensee.getNumber(), licenseTemplateIterator.next(), null,
                        newLicense);
            }

            persistPurchaseLicenseeMapping(licensee.getNumber(), purchaseId);
            removeExpiredPurchaseLicenseeMappings();

            return licensee.getNumber();
        } catch (final NetLicensingException e) {
            throw new BadRequestException("Incorrect data");
        }
    }

    private void persistPurchaseLicenseeMapping(final String licenseeNumber, final String purchaseId) {
        MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository.findFirstByPurchaseId(purchaseId);
        if (myCommercePurchase == null) {
            myCommercePurchase = new MyCommercePurchase();
            myCommercePurchase.setLicenseeNumber(licenseeNumber);
            myCommercePurchase.setPurchaseId(purchaseId);
        }
        myCommercePurchase.setTimestamp(new Date());
        myCommercePurchaseRepository.save(myCommercePurchase);
    }

    private void removeExpiredPurchaseLicenseeMappings() {
        if (isTimeOutExpired(Constants.MyCommerce.NEXT_CLEANUP_TAG, Constants.MyCommerce.CLEANUP_PERIOD_MINUTES)) {
            final Calendar earliestPersistTime = Calendar.getInstance();
            earliestPersistTime.add(Calendar.DATE, -Constants.MyCommerce.PERSIST_PURCHASE_DAYS);
            myCommercePurchaseRepository.deleteByTimestampBefore(earliestPersistTime.getTime());
        }
    }

    private void addCustomPropertiesToLicensee(final MultivaluedMap<String, String> formParams,
            final Licensee licensee) {
        for (final Map.Entry<String, List<String>> entry : formParams.entrySet()) {
            if (!LicenseeImpl.getReservedProps().contains(entry.getKey()) && !entry.getValue().get(0).equals("")) {
                licensee.addProperty(entry.getKey(), entry.getValue().get(0));
            }
        }
    }

    private void checkLicenseTemplates(final Context context, final List<String> licenseTemplateList) {
        final Iterator<String> licenseTemplateIterator = licenseTemplateList.iterator();
        while (licenseTemplateIterator.hasNext()) {
            try {
                LicenseTemplateService.get(context, licenseTemplateIterator.next());
            } catch (final NetLicensingException e) {
                throw new BadRequestException("Incorrect License Template");
            }
        }
    }

    private Licensee getExistingLicensee(final Context context, String licenseeNumber, final String purchaseId) {
        Licensee licensee = null;
        if (StringUtils.isBlank(licenseeNumber)) { // ADD[LICENSEENUMBER] is not provided, get from database
            final MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository
                    .findFirstByPurchaseId(purchaseId);
            if (myCommercePurchase != null) {
                licenseeNumber = myCommercePurchase.getLicenseeNumber();
            }
        }
        if (StringUtils.isNotBlank(licenseeNumber)) {
            try {
                licensee = LicenseeService.get(context, licenseeNumber);
            } catch (final NetLicensingException e) {
                throw new BadRequestException("Licensee number is not correct");
            }
        }
        return licensee;
    }
}
