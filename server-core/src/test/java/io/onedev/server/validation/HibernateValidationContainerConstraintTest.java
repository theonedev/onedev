package io.onedev.server.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;

import org.junit.jupiter.api.Test;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.DependsOn;

public class HibernateValidationContainerConstraintTest extends HibernateValidationTestSupport {

	@Test
	public void elementConstraintsRunBeforeClassValidationInAllGroups() {
		for (Class<?> group: new Class<?>[] {Default.class, Checks.class}) {
			var bean = new Names();
			assertPaths(validator.validate(bean, group), "names[0].<list element>");
			assertEquals(0, bean.calls);
			assertPaths(validator.validateProperty(bean, "names", group), "names[0].<list element>");
			assertPaths(validator.validateValue(Names.class, "names", bean.names, group), "names[0].<list element>");
			bean.names = List.of("valid");
			assertPaths(validator.validate(bean, group));
			assertEquals(1, bean.calls);
		}
	}

	@Test
	public void nestedFieldContainerElementsAreValidated() {
		assertPaths(validator.validate(new NestedField()), "children[key].<map value>[0].<list element>");
	}

	@Test
	public void elementConstraintsRespectGetterOverrides() {
		assertPaths(validator.validate(new Parent()), "names[0].<list element>");
		assertPaths(validator.validate(new Inherited()), "names[0].<list element>");
		assertPaths(validator.validate(new Redeclared()), "names[0].<list element>");
		assertPaths(validator.validate(new Removed()));
	}

	@Test
	public void elementConstraintsRespectPropertyVisibility() {
		var bean = new Dependent();
		assertPaths(validator.validate(bean));
		bean.enabled = true;
		assertPaths(validator.validate(bean), "names[0].<list element>");
	}

	public interface Checks {
	}

	@ClassValidating(groups = {Default.class, Checks.class})
	public static class Names implements Validatable {
		List<String> names = Collections.singletonList(null);
		int calls;

		public List<@NotNull(groups = {Default.class, Checks.class}) String> getNames() {
			return names;
		}

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
			calls++;
			return true;
		}
	}

	public static class NestedField {
		private final Map<String, List<@NotNull String>> children = Map.of("key", Collections.singletonList(null));

		public Map<String, List<String>> getChildren() {
			return children;
		}
	}

	public static class Parent {
		public List<@NotNull String> getNames() {
			return Collections.singletonList(null);
		}
	}

	public static class Inherited extends Parent {
	}

	public static class Redeclared extends Parent {
		@Override
		public List<@NotNull String> getNames() {
			return super.getNames();
		}
	}

	public static class Removed extends Parent {
		@Override
		public List<String> getNames() {
			return super.getNames();
		}
	}

	public static class Dependent {
		boolean enabled;

		public boolean isEnabled() {
			return enabled;
		}

		@DependsOn(property = "enabled")
		public List<@NotNull String> getNames() {
			return Collections.singletonList(null);
		}
	}
}
