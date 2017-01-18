package com.labs64.netlicensing.gateway.controller.restful;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.labs64.netlicensing.domain.entity.License;
import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.impl.LicenseImpl;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.LicenseType;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.controller.restful.exception.MyCommerceException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCommerceController.class);

    @Inject
    private MyCommercePurchaseRepository myCommercePurchaseRepository;

    @POST
    @Path("/" + Constants.MyCommerce.ENDPOINT_PATH_CODEGEN + "/{" + Constants.MyCommerce.PRODUCT_NUMBER + "}")
    @Transactional
    public String codeGenerator(@PathParam(Constants.MyCommerce.PRODUCT_NUMBER) final String productNumber,
            @QueryParam(Constants.MyCommerce.LICENSE_TEMPLATE_NUMBER) final List<String> licenseTemplateList,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.MULTIPLE_LICENSEE) final boolean multipleLicenseeMode,
            @DefaultValue("false") @QueryParam(Constants.MyCommerce.SAVE_USER_DATA) final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) {
        try {
            final Context context = getSecurityHelper().getContext();
            LOGGER.info("MyCommerce Code Generator was started! With Product number: " + productNumber
                    + ", licenseTemplateList: " + licenseTemplateList.toString() + ", formParams: "
                    + formParams.toString());

            final List<String> licensees = new ArrayList<String>();
            final String quantity = formParams.getFirst(Constants.MyCommerce.QUANTITY);
            final String licenseeNumber = formParams.getFirst(Constants.MyCommerce.LICENSEE_NUMBER);
            if (formParams.isEmpty() || licenseTemplateList.isEmpty()) {
                throw new MyCommerceException("Required parameters not provided");
            } else if (multipleLicenseeMode && licenseeNumber != null && !licenseeNumber.isEmpty()) {
                throw new MyCommerceException(
                        "Wrong configuration! Multiple Licensee mode is on, LICENSEENUMBER is passed");
            } else if (quantity == null || quantity.isEmpty() || Integer.parseInt(quantity) < 1) {
                throw new MyCommerceException("Quantity is wrong");
            }

            final Product product = ProductService.get(context, productNumber);
            final Map<String, LicenseTemplate> licenseTemplates = getLicenseTemplates(context, licenseTemplateList);
            Licensee licensee = new LicenseeImpl();
            boolean isNeedCreateNewLicensee = true;

            final String purchaseId = formParams.getFirst(Constants.MyCommerce.PURCHASE_ID);
            // try to get existing Licensee
            if (!multipleLicenseeMode) {
                licensee = getExistingLicensee(context, licenseeNumber, purchaseId, productNumber);
                // if license template and licensee are bound to different products, need to create new licensee
                isNeedCreateNewLicensee = isNeedCreateNewLicensee(licensee, productNumber);
            }

            // create licenses
            for (int i = 1; i <= Integer.parseInt(quantity); i++) {
                // create new Licensee, if not existing or multipleLicenseeMode
                if (licensee == null || isNeedCreateNewLicensee || multipleLicenseeMode) {
                    isNeedCreateNewLicensee = false;
                    licensee = new LicenseeImpl();
                    if (isSaveUserData) {
                        addCustomPropertiesToLicensee(formParams, licensee);
                    }
                    licensee.setActive(true);
                    licensee.setProduct(product);
                    licensee = LicenseeService.create(context, productNumber, licensee);
                }
                for (final LicenseTemplate licenseTemplate : licenseTemplates.values()) {
                    final License newLicense = new LicenseImpl();
                    newLicense.setActive(true);
                    // Required for timeVolume.
                    if (LicenseType.TIMEVOLUME.equals(licenseTemplate.getLicenseType())) {
                        newLicense.addProperty(Constants.PROP_START_DATE, "now");
                    }
                    LicenseService.create(context, licensee.getNumber(), licenseTemplate.getNumber(), null, newLicense);
                }
                if (!licensees.contains(licensee.getNumber())) {
                    licensees.add(licensee.getNumber());
                }
            }
            if (!multipleLicenseeMode) {
                persistPurchaseLicenseeMapping(licensee.getNumber(), purchaseId, productNumber);
                removeExpiredPurchaseLicenseeMappings();
            }
            return String.join(", ", licensees);
        } catch (final NetLicensingException e) {
            throw new MyCommerceException(e.getMessage());
        } catch (final Exception e) {
            throw new MyCommerceException(e.getMessage());
        }
    }

    private boolean isNeedCreateNewLicensee(final Licensee licensee, final String productNumber) {
        boolean isNeedCreateNewLicensee = false;
        if (licensee != null) {
            if (!licensee.getProduct().getNumber().equals(productNumber)) {
                isNeedCreateNewLicensee = true;
            }
        } else {
            isNeedCreateNewLicensee = true;
        }
        return isNeedCreateNewLicensee;
    }

    private void persistPurchaseLicenseeMapping(final String licenseeNumber, final String purchaseId,
            final String productNumber) {
        MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository
                .findFirstByPurchaseIdAndProductNumber(purchaseId, productNumber);
        if (myCommercePurchase == null) {
            myCommercePurchase = new MyCommercePurchase();
            myCommercePurchase.setLicenseeNumber(licenseeNumber);
            myCommercePurchase.setPurchaseId(purchaseId);
            myCommercePurchase.setProductNumber(productNumber);
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

    private Map<String, LicenseTemplate> getLicenseTemplates(final Context context,
            final List<String> licenseTemplateList)
                    throws MyCommerceException, NetLicensingException {
        final Map<String, LicenseTemplate> licenseTemplates = new HashMap<String, LicenseTemplate>();
        final Iterator<String> licenseTemplateIterator = licenseTemplateList.iterator();
        while (licenseTemplateIterator.hasNext()) {
            final LicenseTemplate licenseTemplate = LicenseTemplateService.get(context,
                    licenseTemplateIterator.next());
            licenseTemplates.put(licenseTemplate.getNumber(), licenseTemplate);
        }
        return licenseTemplates;
    }

    private Licensee getExistingLicensee(final Context context, String licenseeNumber, final String purchaseId,
            final String productNumber)
                    throws MyCommerceException, NetLicensingException {
        Licensee licensee = null;
        if (StringUtils.isBlank(licenseeNumber)) { // ADD[LICENSEENUMBER] is not provided, get from database
            final MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository
                    .findFirstByPurchaseIdAndProductNumber(purchaseId, productNumber);
            if (myCommercePurchase != null) {
                licenseeNumber = myCommercePurchase.getLicenseeNumber();
                LOGGER.info("licenseeNumber obtained from repository: " + licenseeNumber);
            }
        }
        if (StringUtils.isNotBlank(licenseeNumber)) {
            licensee = LicenseeService.get(context, licenseeNumber);
        }
        return licensee;
    }

}
