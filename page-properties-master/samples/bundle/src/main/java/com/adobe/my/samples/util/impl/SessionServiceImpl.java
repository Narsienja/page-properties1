package com.adobe.my.samples.util.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.my.samples.util.SessionService;

@Component(immediate = true, service = SessionService.class)
public class SessionServiceImpl implements SessionService {


    private static final String READ_SERVICE = "ReadService";
    private static final String WRITE_SERVICE = "WriteService";
 private static final Logger LOGGER = LoggerFactory.getLogger(SessionServiceImpl.class);
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

     /**
     * It returns a new ResourceResolver instance with the privileges of USER mapped to Read SubService.
     * It also use further configuration taken from the given authenticationInfo map.
     * These authenticationInfo map setting are bind with configuration setting done in 'Apache Sling Service User Mapper Service' in OSGI Config.
     *
     * @return A ResourceResolver with read permissions to execute the service.
     */
    @Override
    public ResourceResolver getReadServiceResourceResolver() throws LoginException {
        ResourceResolver resourceResolver = getServiceResourceResolver(READ_SERVICE);
        if (resourceResolver == null) {
            resourceResolver = getAdministrativeResourceResolver();
        }
        return resourceResolver;
    }

    /**
     * It returns a new ResourceResolver instance with the privileges of USER mapped to Write SubService.
     * It also use further configuration taken from the given authenticationInfo map.
     * These authenticationInfo map setting are bind with configuration setting done in 'Apache Sling Service User Mapper Service' in OSGI Config.
     *
     * @return A ResourceResolver with write permissions to execute the service.
     */
    @Override
    public ResourceResolver getWriteServiceResourceResolver() throws LoginException {
        ResourceResolver resourceResolver = getServiceResourceResolver(WRITE_SERVICE);
        if (resourceResolver == null) {
            resourceResolver = getAdministrativeResourceResolver();
        }
        return resourceResolver;
    }

    /**
     * This Method return the resource resolver for sub service passed.
     *
     * @param subServiceName, Name of the subService
     * @return A ResourceResolver with appropriate permissions to execute the service.
     */
    private ResourceResolver getServiceResourceResolver(String subServiceName) throws LoginException {
   
            Map<String, Object> serviceMap = new HashMap<>();
            serviceMap.put(ResourceResolverFactory.SUBSERVICE, subServiceName);
            return resourceResolverFactory.getServiceResourceResolver(serviceMap);
    }
    
    private ResourceResolver getAdministrativeResourceResolver(){
    	try {
			return resourceResolverFactory
			.getAdministrativeResourceResolver(null);
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }

   


}