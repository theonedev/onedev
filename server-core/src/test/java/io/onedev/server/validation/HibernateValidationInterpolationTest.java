package io.onedev.server.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.junit.Test;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.step.CreateTagStep;

public class HibernateValidationInterpolationTest extends HibernateValidationTestSupport {

	@Test
	public void validatesTagNamesAfterExpandingJobVariablesAndEscapedAtSigns() {
		// ConstraintTree + MetaConstraint: #222 (literal @@), #1242 (tag name validation).
		var step = new CreateTagStep();
		for (String name : List.of("release-@build_number@", "release@@candidate")) {
			step.setTagName(name);
			assertPaths(validator.validateProperty(step, "tagName"));
			assertEquals(name, step.getTagName());
		}
		step.setTagName("release bad-@build_number@");
		var violations = validator.validateProperty(step, "tagName");
		assertPaths(violations, "tagName");
		assertEquals("Invalid git tag name", violations.iterator().next().getMessage());
		assertEquals(step.getTagName(), violations.iterator().next().getInvalidValue());
	}

	@Test
	public void usesTheAnnotationExampleInsteadOfResolvingRuntimeVariables() {
		var bean = new NumericSetting();
		bean.value = "@param:port@";
		assertPaths(validator.validate(bean));
		assertEquals("@param:port@", bean.value);
		bean.value = "invalid-@param:port@";
		assertPaths(validator.validate(bean), "value");
		bean.value = null;
		assertPaths(validator.validate(bean), "value");
	}

	@Test
	public void malformedInterpolationIsReportedOnlyByTheInterpolationValidator() {
		var bean = new NumericSetting();
		bean.value = "@param:port";
		var violations = validator.validate(bean);
		assertPaths(violations, "value");
		assertEquals(Interpolative.class, violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType());
		assertNull(MetaConstraint.get());
	}

	@Test
	public void collectionConstraintsSeeExpandedValuesWithoutChangingTheBean() {
		var bean = new NumericList();
		bean.values = List.of("@param:first@", "12");
		assertPaths(validator.validate(bean));
		assertEquals(List.of("@param:first@", "12"), bean.values);
		bean.values = List.of("@param:first@", "bad");
		assertPaths(validator.validate(bean), "values");
		bean.values = List.of("@param:first");
		var violations = validator.validate(bean);
		assertPaths(violations, "values");
		assertEquals(Interpolative.class, violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType());
	}

	@Test
	public void nestedValidationRestoresTheEnclosingConstraint() {
		// A custom validator may validate another bean; interpolation must use the inner
		// getter's metadata, then restore the caller's metadata even on failure.
		assertNull(MetaConstraint.get());
		var probe = new ValidationProbe();
		probe.action = () -> {
			var enclosing = MetaConstraint.get();
			assertEquals(ClassValidating.class, enclosing.getDescriptor().getAnnotationType());
			var bean = new NumericSetting();
			bean.value = "@param:port@";
			assertPaths(validator.validate(bean));
			assertSame(enclosing, MetaConstraint.get());
			var broken = new ValidationProbe();
			broken.action = () -> { throw new IllegalStateException("validator failed"); };
			assertThrows(ValidationException.class, () -> validator.validate(broken));
			assertSame(enclosing, MetaConstraint.get());
		};
		assertPaths(validator.validate(probe));
		assertNull(MetaConstraint.get());
	}

	@Test
	public void validatorExceptionsDoNotLeakConstraintMetadata() {
		var probe = new ValidationProbe();
		probe.action = () -> { throw new IllegalStateException("validator failed"); };
		assertThrows(ValidationException.class, () -> validator.validate(probe));
		assertNull(MetaConstraint.get());
		var bean = new NumericSetting();
		bean.value = "@param:port@";
		assertPaths(validator.validate(bean));
		assertNull(MetaConstraint.get());
	}

	@Test
	public void concurrentValidationDoesNotShareConstraintMetadata() {
		var executor = Executors.newSingleThreadExecutor();
		try {
			var probe = new ValidationProbe();
			probe.action = () -> {
				var enclosing = MetaConstraint.get();
				try {
					executor.submit(() -> {
						assertNull(MetaConstraint.get());
						var bean = new NumericSetting();
						bean.value = "@param:port@";
						assertPaths(validator.validate(bean));
						assertNull(MetaConstraint.get());
					}).get(5, TimeUnit.SECONDS);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
				assertSame(enclosing, MetaConstraint.get());
			};
			assertPaths(validator.validate(probe));
			assertNull(MetaConstraint.get());
		} finally {
			executor.shutdownNow();
		}
	}

	public static class NumericSetting {
		String value;

		@NotNull
		@Interpolative(exampleVar = "42")
		@Pattern(regexp = "[0-9]+")
		public String getValue() {
			return value;
		}
	}

	public static class NumericList {
		List<String> values;

		@Interpolative(exampleVar = "42")
		@NumericElements
		public List<String> getValues() {
			return values;
		}
	}

	@Target(java.lang.annotation.ElementType.METHOD)
	@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = NumericElementsValidator.class)
	public @interface NumericElements {
		String message() default "Expected numeric elements";
		Class<?>[] groups() default {};
		Class<? extends Payload>[] payload() default {};
	}

	public static class NumericElementsValidator implements ConstraintValidator<NumericElements, List<String>> {
		@Override
		public boolean isValid(List<String> value, ConstraintValidatorContext context) {
			return value == null || value.stream().allMatch(it -> it.matches("[0-9]+"));
		}
	}

	@ClassValidating
	public static class ValidationProbe implements Validatable {
		Runnable action;

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
			action.run();
			return true;
		}
	}
}
