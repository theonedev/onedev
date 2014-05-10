package com.pmease.commons.validation;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

public class ValidatorFactoryProvider implements Provider<ValidatorFactory> {

	private final Set<ValidationConfigurator> configurators;
	
	@Inject
	public ValidatorFactoryProvider(Set<ValidationConfigurator> configurators) {
		this.configurators = configurators;
	}
	
	@Override
	public ValidatorFactory get() {
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		for (ValidationConfigurator configurator: configurators)
			configurator.configure(configuration);
		return configuration.buildValidatorFactory();
	}

}
