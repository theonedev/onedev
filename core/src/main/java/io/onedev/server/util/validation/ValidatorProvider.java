package io.onedev.server.util.validation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

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
