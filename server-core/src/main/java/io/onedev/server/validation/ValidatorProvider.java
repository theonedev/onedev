package io.onedev.server.validation;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ValidatorProvider implements Provider<Validator> {

	private final ValidatorFactory validatorFactory;
	
	@Inject
	public ValidatorProvider(ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}
	
	@Override
	public Validator get() {
		return validatorFactory.getValidator();
	}

}
