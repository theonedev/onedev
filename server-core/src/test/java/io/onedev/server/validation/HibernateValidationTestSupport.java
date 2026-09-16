package io.onedev.server.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class HibernateValidationTestSupport {

	private ValidatorFactory factory;

	protected Validator validator;

	@BeforeEach
	public void createValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterEach
	public void closeValidator() {
		factory.close();
	}

	protected void assertPaths(Set<? extends ConstraintViolation<?>> violations, String... expected) {
		assertEquals(Arrays.stream(expected).collect(Collectors.toSet()), violations.stream()
				.map(it -> it.getPropertyPath().toString()).collect(Collectors.toSet()));
		assertEquals(expected.length, violations.size(), "Unexpected duplicate violations: " + violations);
	}
}
