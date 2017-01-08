package com.labs64.netlicensing.gateway.util;

public class Constants {

    private Constants() {
    }

    public static final String PROP_START_DATE = "startDate";

    public static final class MyCommerce {
        public static final String NEXT_CLEANUP_TAG = "MyCommerceNextCleanup";
        public static final int CLEANUP_PERIOD_MINUTES = 60;

        public static final int PERSIST_PURCHASE_DAYS = 3;

        public static final String ENDPOINT_BASE_PATH = "mycommerce";
        public static final String ENDPOINT_PATH_CODEGEN = "codegen";
        public static final String PRODUCT_NUMBER = "productNumber";
        public static final String LICENSE_TEMPLATE_NUMBER = "licenseTemplateNumber";
        public static final String SAVE_USER_DATA = "saveUserData";

        /** myCommerce purchase id */
        public static final String PURCHASE_ID = "PURCHASE_ID";
        /** increments for each product for multiple products in one purchase */
        public static final String RUNNING_NO = "RUNNING_NO";
        public static final String PURCHASE_DATE = "PURCHASE_DATE";
        /** ID of the product purchased */
        public static final String PRODUCT_ID = "PRODUCT_ID";
        public static final String QUANTITY = "QUANTITY";
        /** The name to which the customer chose to license the product */
        public static final String REG_NAME = "REG_NAME";
        /** 1st Customizable Field */
        public static final String ADDITIONAL1 = "ADDITIONAL1";
        /** 2nd Customizable Field */
        public static final String ADDITIONAL2 = "ADDITIONAL2";
        /** The name of the reseller or affiliate involved in this order */
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
         * <not present> = ISO-8859-1 (Latin 1) encoding<br/>
         * "UTF8" = UTF8 Unicode
         */
        public static final String ENCODING = "ENCODING";
        public static final String LANGUAGE_ID = "LANGUAGE_ID";
        /** Name of the promotion */
        public static final String PROMOTION_NAME = "PROMOTION_NAME";
        /** The actual promotion coupon code used for this order */
        public static final String PROMOTION_COUPON_CODE = "PROMOTION_COUPON_CODE";
        /** Date of Subscription */
        public static final String SUBSCRIPTION_DATE = "SUBSCRIPTION_DATE";
        /** Start date of current re-billing period */
        public static final String START_DATE = "START_DATE";
        /** Expiry date (date of next re-billing) */
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
        /** custom field from myCommerce (licenseeNumber) */
        public static final String LICENSEE_NUMBER = "ADD[LICENSEENUMBER]";

    }
}
