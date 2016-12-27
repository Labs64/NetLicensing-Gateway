package com.labs64.netlicensing.gateway.util.security;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;

/**
 * Utility class SecurityHelper contains helper methods for the Spring security context.
 */
public final class SecurityHelper implements ApplicationContextAware {

    static final String REST_API_PATH = "/core/v2/rest";
    static final String BASE_URL_UNITTEST = "http://localhost:28080";
    static final String BASE_URL_PROD = "https://go.netlicensing.io";

    static final String BASE_URL = BASE_URL_UNITTEST + REST_API_PATH;

    private ApplicationContext springContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        springContext = applicationContext;
    }

    public Context getContext() {
        final Context context = new Context();
        context.setBaseUrl(BASE_URL);
        context.setSecurityMode(SecurityMode.BASIC_AUTHENTICATION);

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication instanceof AnonymousAuthenticationToken) {
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
