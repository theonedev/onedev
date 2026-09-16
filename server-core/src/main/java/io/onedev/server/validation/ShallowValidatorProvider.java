package io.onedev.server.validation;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

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
