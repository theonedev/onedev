package com.gitplex.commons.validation;

import javax.inject.Singleton;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.gitplex.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ValidationModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(ValidatorFactory.class).toProvider(ValidatorFactoryProvider.class).in(Singleton.class);
		bind(Validator.class).toProvider(ValidatorProvider.class).in(Singleton.class);
		
		contribute(ValidationConfigurator.class, DummyValidationConfigurator.class);
	}

}
