package io.onedev.server.validation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import io.onedev.server.annotation.Shallow;

public class ShallowValidatorProvider implements Provider<Validator> {

	private final ValidatorFactory validatorFactory;
	
	@Inject
	public ShallowValidatorProvider(@Shallow ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}
	
	@Override
	public Validator get() {
		return validatorFactory.getValidator();
	}

}
