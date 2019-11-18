package com.labs64.netlicensing.gateway.util;

public class Constants {

    private Constants() {
    }

    public static final int CLEANUP_PERIOD_MINUTES = 60;
    public static final String LOG_NEXT_CLEANUP_TAG = "LogNextCleanup";
    public static final int LOG_PERSIST_DAYS = 30;

    public static final class NetLicensing {
        public static final String PRODUCT_NUMBER = "productNumber";
        public static final String LICENSE_TEMPLATE_NUMBER = "licenseTemplateNumber";
        public static final String LICENSEE_NUMBER = "licenseeNumber";
        public static final String PROP_MARKED_FOR_TRANSFER = "markedForTransfer";
        public static final String PROP_START_DATE = "startDate";
    }

    public static final class Monitoring {
        public static final String ENDPOINT_BASE_PATH = "monitoring";
    }
}
