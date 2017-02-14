package com.gitplex.commons.jersey;

import javax.validation.ValidatorFactory;
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.server.validation.ValidationConfig;

import com.gitplex.calla.loader.AppLoader;

public class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

    @Override
    public ValidationConfig getContext(final Class<?> type) {
    	ValidatorFactory factory = AppLoader.getInstance(ValidatorFactory.class);
        ValidationConfig config = new ValidationConfig();
        config.constraintValidatorFactory(factory.getConstraintValidatorFactory());
        config.messageInterpolator(factory.getMessageInterpolator());
        config.parameterNameProvider(factory.getParameterNameProvider());
        config.traversableResolver(factory.getTraversableResolver());
        return config;
    }

}