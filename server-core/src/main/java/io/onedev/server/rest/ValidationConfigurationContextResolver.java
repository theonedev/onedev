package io.onedev.server.rest;

import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

import io.onedev.commons.loader.AppLoader;

@Provider
public class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

    @Context
    private ResourceContext resourceContext;
    
    @Override
    public ValidationConfig getContext(final Class<?> type) {
    	ValidatorFactory factory = AppLoader.getInstance(ValidatorFactory.class);
        ValidationConfig config = new ValidationConfig();
        config.constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class));                
        config.messageInterpolator(factory.getMessageInterpolator());
        config.parameterNameProvider(factory.getParameterNameProvider());
        config.traversableResolver(factory.getTraversableResolver());
        return config;
    }

}