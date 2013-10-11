package com.pmease.commons.validation;

import javax.validation.Validation;
import javax.validation.Validator;

import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ValidationModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(Validator.class).toInstance(Validation.buildDefaultValidatorFactory().getValidator());
	}

}
