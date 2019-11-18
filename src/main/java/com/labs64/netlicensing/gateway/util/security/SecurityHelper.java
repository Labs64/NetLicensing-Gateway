package com.labs64.netlicensing.gateway.util.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.provider.RestProvider;
import com.labs64.netlicensing.service.UtilityService;

/**
 * Utility class SecurityHelper contains helper methods for the Spring security context.
 */
public final class SecurityHelper {

    private String nlicBaseUrl;
    private String nlicDemoUser;
    private String nlicDemoPass;

    public class GWClientConfiguration implements RestProvider.Configuration {

        @Override
        public String getUserAgent() {
            return "NetLicensing/Java " + System.getProperty("java.version") + " (Gateway)";
        }

        @Override
        public boolean isLoggingEnabled() {
            return false;
        }

    }

    @Required
    public void setNlicBaseUrl(final String nlicBaseUrl) {
        this.nlicBaseUrl = nlicBaseUrl;
    }

    @Required
    public void setNlicDemoUser(final String nlicDemoUser) {
        this.nlicDemoUser = nlicDemoUser;
    }

    @Required
    public void setNlicDemoPass(final String nlicDemoPass) {
        this.nlicDemoPass = nlicDemoPass;
    }

    public Context getContext() {
        final Context context = new Context();
        context.setBaseUrl(nlicBaseUrl);
        context.setSecurityMode(SecurityMode.BASIC_AUTHENTICATION);
        context.setObject(RestProvider.Configuration.class, new GWClientConfiguration());

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication instanceof AnonymousAuthenticationToken) {
                // TODO(2K): handle missing authentication (no cases so far)
                context.setUsername("");
                context.setPassword("");
            } else {
                context.setUsername(authentication.getPrincipal().toString());
                context.setPassword(authentication.getCredentials().toString());
            }
        }
        return context;
    }

    public Context getDemoContext() {
        final Context context = new Context();
        context.setBaseUrl(nlicBaseUrl);
        context.setSecurityMode(SecurityMode.BASIC_AUTHENTICATION);
        context.setUsername(nlicDemoUser);
        context.setPassword(nlicDemoPass);
        context.setObject(RestProvider.Configuration.class, new GWClientConfiguration());
        return context;
    }

    public static boolean checkContextConnection(final Context context) {
        try {
            UtilityService.listLicenseTypes(context);
        } catch (NetLicensingException e) {
            return false;
        }
        return true;
    }

}
