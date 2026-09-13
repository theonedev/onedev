package io.onedev.server.validation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.junit.Test;

import io.onedev.server.annotation.ClassValidating;

public class HibernateValidationOrderTest extends HibernateValidationTestSupport {

	@Test
	public void classValidationWaitsForPropertiesAndCascadedBeans() {
		// BuildSpec.isValid runs after nested jobs/steps are validated. Cover the default,
		// explicit group, and group-sequence paths through the customized ValidatorImpl.
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class}) {
			var root = new Root();
			root.name = "job";
			root.child.name = "step";
			assertPaths(validator.validate(root, group));
			assertEquals(List.of("child", "root"), root.calls);
		}
	}

	@Test
	public void invalidPropertySuppressesClassValidationOnlyOutsideTheDefaultGroup() {
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class}) {
			var root = new Root();
			root.child.name = "step";
			assertPaths(validator.validate(root, group), "name");
			// The default-group path bypasses the existing failure guard.
			assertEquals(group == Default.class ? List.of("child", "root") : List.of(), root.calls);
		}
	}

	@Test
	public void invalidNestedPropertySuppressesClassValidationOnlyOutsideTheDefaultGroup() {
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class}) {
			var root = new Root();
			root.name = "job";
			assertPaths(validator.validate(root, group), "child.name");
			assertEquals(group == Default.class ? List.of("child", "root") : List.of(), root.calls);
		}
	}

	@Test
	public void nestedClassViolationSuppressesParentValidationOnlyOutsideTheDefaultGroup() {
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class, OrderedChecks.class}) {
			var root = new Root();
			root.name = "job";
			root.child.name = "step";
			root.child.valid = false;
			assertPaths(validator.validate(root, group), "child");
			assertEquals(group == Default.class ? List.of("child", "root") : List.of("child"), root.calls);
		}
	}

	public interface Checks {
	}

	@GroupSequence({Checks.class, Default.class})
	public interface OrderedChecks {
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
