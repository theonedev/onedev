package io.onedev.server.validation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.After;
import org.junit.Before;

public abstract class HibernateValidationTestSupport {

	private ValidatorFactory factory;

	protected Validator validator;

	@Before
	public void createValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@After
	public void closeValidator() {
		factory.close();
	}

	protected void assertPaths(Set<? extends ConstraintViolation<?>> violations, String... expected) {
		assertEquals(Arrays.stream(expected).collect(Collectors.toSet()), violations.stream()
				.map(it -> it.getPropertyPath().toString()).collect(Collectors.toSet()));
		assertEquals("Unexpected duplicate violations: " + violations, expected.length, violations.size());
	}
}
