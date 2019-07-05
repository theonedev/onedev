package io.onedev.server.rest.jersey;

import javax.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import io.onedev.commons.launcher.loader.AppLoader;

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
        register(JacksonFeature.class);
        
        packages(JerseyApplication.class.getPackage().getName());
        
	    for (JerseyConfigurator configurator: AppLoader.getExtensions(JerseyConfigurator.class)) {
	    	configurator.configure(this);
	    }
	}
	
}
