package com.adobe.my.samples.util;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Session;


public interface SessionService {	
    ResourceResolver getReadServiceResourceResolver() throws LoginException;
    ResourceResolver getWriteServiceResourceResolver() throws LoginException;
}