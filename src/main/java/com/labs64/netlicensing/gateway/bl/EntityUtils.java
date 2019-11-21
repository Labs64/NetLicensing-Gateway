package com.labs64.netlicensing.gateway.bl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.labs64.netlicensing.domain.entity.LicenseTemplate;
import com.labs64.netlicensing.domain.entity.Licensee;
import com.labs64.netlicensing.domain.entity.impl.LicenseeImpl;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseTemplateService;

public class EntityUtils {

    public static Map<String, LicenseTemplate> getLicenseTemplates(final Context context,
            final List<String> licenseTemplateList) throws NetLicensingException {
        final Map<String, LicenseTemplate> licenseTemplates = new HashMap<>();
        for (String number : licenseTemplateList) {
            final LicenseTemplate licenseTemplate = LicenseTemplateService.get(context, number);
            licenseTemplates.put(licenseTemplate.getNumber(), licenseTemplate);
        }
        return licenseTemplates;
    }

    public static void addCustomPropertiesToLicensee(final MultivaluedMap<String, String> formParams,
            final Licensee licensee) {
        for (final Map.Entry<String, List<String>> entry : formParams.entrySet()) {
            if (!LicenseeImpl.getReservedProps().contains(entry.getKey()) && !entry.getValue().get(0).equals("")) {
                licensee.addProperty(entry.getKey(), entry.getValue().get(0));
            }
        }
    }

}
