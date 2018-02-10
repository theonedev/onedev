package com.turbodev.server.rest.jersey;

import javax.inject.Inject;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.turbodev.launcher.loader.AppLoader;

public class JerseyApplication extends ResourceConfig {

	@Inject
	public JerseyApplication(ServiceLocator serviceLocator) {
		GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
		GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
	    guiceBridge.bridgeGuiceInjector(AppLoader.injector);
	    
	    String disableMoxy = PropertiesHelper.getPropertyNameForRuntime(
	    		CommonProperties.MOXY_JSON_FEATURE_DISABLE,
                getConfiguration().getRuntimeType());
        property(disableMoxy, true);
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        // add the default Jackson exception mappers
        register(JsonParseExceptionMapper.class);
        register(JsonMappingExceptionMapper.class);
        register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
        
        packages(JerseyApplication.class.getPackage().getName());
        
	    for (JerseyConfigurator configurator: AppLoader.getExtensions(JerseyConfigurator.class)) {
	    	configurator.configure(this);
	    }
	}
	
}
