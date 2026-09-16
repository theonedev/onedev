package io.onedev.server.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;

import org.junit.jupiter.api.Test;

import io.onedev.server.annotation.ClassValidating;

public class HibernateValidationOrderTest extends HibernateValidationTestSupport {

	@Test
	public void classValidationWaitsForPropertiesAndCascadedBeans() {
		// BuildSpec.isValid runs after nested jobs/steps are validated. Cover the default,
		// explicit group, and group-sequence paths through the customized ValidatorImpl.
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class, DefaultFirstChecks.class}) {
			var root = new Root();
			root.name = "job";
			root.child.name = "step";
			assertPaths(validator.validate(root, group));
			assertEquals(List.of("child", "root"), root.calls);
		}
	}

	@Test
	public void invalidPropertySuppressesClassValidation() {
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class, DefaultFirstChecks.class}) {
			var root = new Root();
			root.child.name = "step";
			assertPaths(validator.validate(root, group), "name");
			assertEquals(List.of(), root.calls);
		}
	}

	@Test
	public void invalidNestedPropertySuppressesClassValidation() {
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class, DefaultFirstChecks.class}) {
			var root = new Root();
			root.name = "job";
			assertPaths(validator.validate(root, group), "child.name");
			assertEquals(List.of(), root.calls);
		}
	}

	@Test
	public void nestedClassViolationSuppressesParentValidation() {
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class, DefaultFirstChecks.class}) {
			var root = new Root();
			root.name = "job";
			root.child.name = "step";
			root.child.valid = false;
			assertPaths(validator.validate(root, group), "child");
			assertEquals(List.of("child"), root.calls);
		}
	}

	@Test
	public void ungroupedClassValidationWaitsForSuccessfulPropertyValidation() {
		var bean = new UngroupedBean();
		assertPaths(validator.validate(bean), "name");
		assertEquals(0, bean.calls);
		bean.name = "job";
		assertPaths(validator.validate(bean));
		assertEquals(1, bean.calls);
	}

	@ClassValidating
	public static class UngroupedBean implements Validatable {
		String name;
		int calls;

		@NotNull
		public String getName() {
			return name;
		}

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
			calls++;
			return true;
		}
	}

	public interface Checks {
	}

	@GroupSequence({Checks.class, Default.class})
	public interface OrderedChecks {
	}

	@GroupSequence({Default.class, Checks.class})
	public interface DefaultFirstChecks {
	}

	@ClassValidating(groups = {Default.class, Checks.class})
	public static class Root implements Validatable {
		final List<String> calls = new ArrayList<>();
		final Child child = new Child(calls);
		String name;

		@NotNull(groups = {Default.class, Checks.class})
		public String getName() {
			return name;
		}

		@Valid
		public Child getChild() {
			return child;
		}

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
			calls.add("root");
			return true;
		}
	}

	@ClassValidating(groups = {Default.class, Checks.class}, message = "Invalid step")
	public static class Child implements Validatable {
		final List<String> calls;
		String name;
		boolean valid = true;

		Child(List<String> calls) {
			this.calls = calls;
		}

		@NotNull(groups = {Default.class, Checks.class})
		public String getName() {
			return name;
		}

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
			calls.add("child");
			return valid;
		}
	}
}
