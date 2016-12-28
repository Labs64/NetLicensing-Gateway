package com.labs64.netlicensing.gateway.controller.restful;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;

import com.labs64.netlicensing.gateway.domain.repositories.StoredResponseRepository;
import com.labs64.netlicensing.gateway.util.security.SecurityHelper;

abstract class AbstractBaseController {

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private SecurityHelper securityHelper;

    @Inject
    private StoredResponseRepository storedResponseRepository;

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected SecurityHelper getSecurityHelper() {
        return securityHelper;
    }

    protected StoredResponseRepository getStoredResponseRepository() {
        return storedResponseRepository;
    }
}
