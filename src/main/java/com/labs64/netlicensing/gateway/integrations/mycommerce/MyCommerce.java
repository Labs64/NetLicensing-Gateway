package com.labs64.netlicensing.gateway.integrations.mycommerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.Product;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.gateway.bl.EntityUtils;
import com.labs64.netlicensing.gateway.bl.TimeStampTracker;
import com.labs64.netlicensing.gateway.integrations.common.BaseIntegration;
import com.labs64.netlicensing.gateway.util.Constants;
import com.labs64.netlicensing.service.LicenseeService;
import com.labs64.netlicensing.service.ProductService;

@Component
public class MyCommerce extends BaseIntegration {

    public static final class MyCommerceConstants {
        public static final String NEXT_CLEANUP_TAG = "MyCommerceNextCleanup";

        public static final int PERSIST_PURCHASE_DAYS = 3;

        public static final String ENDPOINT_BASE_PATH = "mycommerce";

        /**
         * myCommerce purchase id
         */
        public static final String PURCHASE_ID = "PURCHASE_ID";
        /**
         * increments for each product for multiple products in one purchase
         */
        public static final String RUNNING_NO = "RUNNING_NO";
        public static final String PURCHASE_DATE = "PURCHASE_DATE";
        /**
         * ID of the product purchased
         */
        public static final String PRODUCT_ID = "PRODUCT_ID";
        public static final String QUANTITY = "QUANTITY";
        /**
         * The name to which the customer chose to license the product
         */
        public static final String REG_NAME = "REG_NAME";
        /**
         * 1st Customizable Field
         */
        public static final String ADDITIONAL1 = "ADDITIONAL1";
        /**
         * 2nd Customizable Field
         */
        public static final String ADDITIONAL2 = "ADDITIONAL2";
        /**
         * The name of the reseller or affiliate involved in this order
         */
        public static final String RESELLER = "RESELLER";

        public static final String LASTNAME = "LASTNAME";
        public static final String FIRSTNAME = "FIRSTNAME";
        public static final String COMPANY = "COMPANY";
        public static final String EMAIL = "EMAIL";
        public static final String PHONE = "PHONE";
        public static final String FAX = "FAX";
        public static final String STREET = "STREET";
        public static final String CITY = "CITY";
        public static final String ZIP = "ZIP";
        public static final String STATE = "STATE";
        public static final String COUNTRY = "COUNTRY";
        /**
         * (not present) = ISO-8859-1 (Latin 1) encoding<br>
         * "UTF8" = UTF8 Unicode
         */
        public static final String ENCODING = "ENCODING";
        public static final String LANGUAGE_ID = "LANGUAGE_ID";
        /**
         * Name of the promotion
         */
        public static final String PROMOTION_NAME = "PROMOTION_NAME";
        /**
         * The actual promotion coupon code used for this order
         */
        public static final String PROMOTION_COUPON_CODE = "PROMOTION_COUPON_CODE";
        /**
         * Date of Subscription
         */
        public static final String SUBSCRIPTION_DATE = "SUBSCRIPTION_DATE";
        /**
         * Start date of current re-billing period
         */
        public static final String START_DATE = "START_DATE";
        /**
         * Expiry date (date of next re-billing)
         */
        public static final String EXPIRY_DATE = "EXPIRY_DATE";
        /**
         * The customer’s two letter country code, For example: US=United States, DE=GERMANY
         */
        public static final String ISO_CODE = "ISO_CODE";
        /**
         * Customer has agreed to receive the publisher's newsletter or not. Possible values: NLALLOW="YES" or "NO"
         */
        public static final String NLALLOW = "NLALLOW";
        /**
         * Payment on invoice for purchase orders. Possible values: INVOICE="UNPAID" or "PAID"
         */
        public static final String INVOICE = "INVOICE";
        /**
         * custom field from myCommerce (licenseeNumber)
         */
        public static final String LICENSEE_NUMBER = "ADD[LICENSEENUMBER]";
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCommerce.class);

    @Autowired
    private TimeStampTracker timeStampTracker;

    @Autowired
    private MyCommercePurchaseRepository myCommercePurchaseRepository;

    public String codeGenerator(final Context context, final String purchaseId, final String productNumber,
            final List<String> licenseTemplateList, final boolean quantityToLicensee, final boolean isSaveUserData,
            final MultivaluedMap<String, String> formParams) throws NetLicensingException {

        logEntries(productNumber, purchaseId, licenseTemplateList, formParams, LOGGER);

        final List<String> licensees = new ArrayList<>();
        if (formParams.isEmpty() || licenseTemplateList.isEmpty()) {
            throw new MyCommerceException("Required parameters not provided");
        }
        final String licenseeNumber = formParams.getFirst(MyCommerce.MyCommerceConstants.LICENSEE_NUMBER);
        if (quantityToLicensee && licenseeNumber != null && !licenseeNumber.isEmpty()) {
            throw new MyCommerceException("'" + MyCommerce.MyCommerceConstants.LICENSEE_NUMBER + "' is not allowed in '"
                    + Constants.QUANTITY_TO_LICENSEE + "' mode");
        }
        final String quantity = formParams.getFirst(MyCommerce.MyCommerceConstants.QUANTITY);
        if (quantity == null || quantity.isEmpty() || Integer.parseInt(quantity) < 1) {
            throw new MyCommerceException("'" + MyCommerce.MyCommerceConstants.QUANTITY + "' invalid or not provided");
        }

        final Product product = ProductService.get(context, productNumber);
        final Map<String, LicenseTemplate> licenseTemplates = EntityUtils.getLicenseTemplates(context,
                licenseTemplateList);
        Licensee licensee = new LicenseeImpl();
        boolean isNeedCreateNewLicensee = true;

        // try to get existing Licensee
        if (!quantityToLicensee) {
            licensee = getExistingLicensee(context, licenseeNumber, purchaseId, productNumber);
            // if license template and licensee are bound to different products, need to create new licensee
            isNeedCreateNewLicensee = isNeedCreateNewLicensee(licensee, productNumber);
        }

        // create licenses
        for (int i = 1; i <= Integer.parseInt(quantity); i++) {
            // create new Licensee, if not existing or multipleLicenseeMode
            if (licensee == null || isNeedCreateNewLicensee || quantityToLicensee) {
                isNeedCreateNewLicensee = false;
                licensee = createLicensee(context, product, (isSaveUserData ? formParams : null));
            }
            createLicenseForLicenseTemplates(context, licenseTemplates, licensee);

            if (!licensees.contains(licensee.getNumber())) {
                licensees.add(licensee.getNumber());
            }
        }
        if (!quantityToLicensee) {
            persistPurchaseLicenseeMapping(licensee.getNumber(), purchaseId, productNumber);
            removeExpiredPurchaseLicenseeMappings();
        }
        return StringUtils.join(licensees, "\n");
    }

    private void persistPurchaseLicenseeMapping(final String licenseeNumber, final String purchaseId,
            final String productNumber) {
        MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository.findFirstByPurchaseIdAndProductNumber(
                purchaseId, productNumber);
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
        if (timeStampTracker.isTimeOutExpired(MyCommerce.MyCommerceConstants.NEXT_CLEANUP_TAG,
                Constants.CLEANUP_PERIOD_MINUTES)) {
            final Calendar earliestPersistTime = Calendar.getInstance();
            earliestPersistTime.add(Calendar.DATE, -MyCommerce.MyCommerceConstants.PERSIST_PURCHASE_DAYS);
            myCommercePurchaseRepository.deleteByTimestampBefore(earliestPersistTime.getTime());
        }
    }

    private Licensee getExistingLicensee(final Context context, String licenseeNumber, final String purchaseId,
            final String productNumber) throws NetLicensingException {
        Licensee licensee = null;
        if (StringUtils.isBlank(licenseeNumber)) { // ADD[LICENSEENUMBER] is not provided, get from database
            final MyCommercePurchase myCommercePurchase = myCommercePurchaseRepository.findFirstByPurchaseIdAndProductNumber(
                    purchaseId, productNumber);
            if (myCommercePurchase != null) {
                licenseeNumber = myCommercePurchase.getLicenseeNumber();
                LOGGER.info("licenseeNumber obtained from repository: {}", licenseeNumber);
            }
        }
        if (StringUtils.isNotBlank(licenseeNumber)) {
            licensee = LicenseeService.get(context, licenseeNumber);
        }
        return licensee;
    }

}
