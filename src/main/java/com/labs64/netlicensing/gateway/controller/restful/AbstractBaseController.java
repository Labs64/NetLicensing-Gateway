package com.labs64.netlicensing.gateway.controller.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.labs64.netlicensing.gateway.util.security.SecurityHelper;

public abstract class AbstractBaseController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SecurityHelper securityHelper;

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected SecurityHelper getSecurityHelper() {
        return securityHelper;
    }

}
