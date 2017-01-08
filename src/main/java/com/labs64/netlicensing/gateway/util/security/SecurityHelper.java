package com.labs64.netlicensing.gateway.util.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;

/**
 * Utility class SecurityHelper contains helper methods for the Spring security context.
 */
public final class SecurityHelper {

    private String nlicBaseUrl;

    @Required
    public void setCoreBaseUrl(final String coreBaseUrl) {
        this.nlicBaseUrl = coreBaseUrl;
    }

    public Context getContext() {
        final Context context = new Context();
        context.setBaseUrl(nlicBaseUrl);
        context.setSecurityMode(SecurityMode.BASIC_AUTHENTICATION);

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

}
