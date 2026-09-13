package io.onedev.server.validation;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.junit.Test;

import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.util.EditContext;

public class HibernateValidationVisibilityTest extends HibernateValidationTestSupport {

	@Test
	public void hiddenPropertiesAndNestedBeansAreNotValidated() {
		var bean = new DependentSettings();
		assertPaths(validator.validate(bean));
		bean.enabled = true;
		assertPaths(validator.validate(bean), "name", "child.name", "children[0].name");
		bean.enabled = false;
		assertPaths(validator.validate(bean));
	}

	@Test
	public void allRepeatedDependenciesMustBeSatisfied() {
		// OD-2884: a setting can depend on more than one privilege or mode.
		var bean = new MultipleDependencies();
		for (boolean enabled : new boolean[] {false, true}) {
			for (String mode : new String[] {"read", "write"}) {
				bean.enabled = enabled;
				bean.mode = mode;
				if (enabled && mode.equals("write"))
					assertPaths(validator.validate(bean), "name", "child.name", "children[0].name");
				else
					assertPaths(validator.validate(bean));
			}
		}
	}

	@Test
	public void showConditionsUseBeanValuesAndRestoreTheOuterEditContext() {
		EditContext outer = name -> "outer value";
		EditContext.push(outer);
		try {
			var bean = new ConditionalSettings();
			assertPaths(validator.validate(bean));
			assertSame(outer, EditContext.get());
			bean.enabled = true;
			assertPaths(validator.validate(bean), "name", "child.name", "children[0].name");
			assertSame(outer, EditContext.get());
		} finally {
			EditContext.pop();
		}
	}

	@Test
	public void showConditionsCanLookUpEditableDisplayNames() {
		// OD-2560: parameter show conditions use display names, not only Java property names.
		var bean = new DisplayNameCondition();
		assertPaths(validator.validate(bean));
		bean.enabled = true;
		assertPaths(validator.validate(bean), "name");
	}

	@Test
	public void failingShowConditionRestoresTheOuterEditContext() {
		EditContext outer = name -> "outer value";
		EditContext.push(outer);
		try {
			assertThrows(RuntimeException.class, () -> validator.validate(new BrokenCondition()));
			assertSame(outer, EditContext.get());
		} finally {
			EditContext.pop();
		}
	}

	public static class Child {
		@NotNull
		public String getName() {
			return null;
		}
	}

	public static class DependentSettings {
		boolean enabled;
		private final Child child = new Child();
		private final List<Child> children = List.of(new Child());

		public boolean isEnabled() {
			return enabled;
		}

		@NotNull
		@DependsOn(property = "enabled")
		public String getName() {
			return null;
		}

		@Valid
		@DependsOn(property = "enabled")
		public Child getChild() {
			return child;
		}

		@Valid
		@DependsOn(property = "enabled")
		public List<Child> getChildren() {
			return children;
		}
	}

	public static class MultipleDependencies extends DependentSettings {
		String mode;

		public String getMode() {
			return mode;
		}

		@Override
		@NotNull
		@DependsOn(property = "enabled")
		@DependsOn(property = "mode", value = "read", inverse = true)
		public String getName() {
			return null;
		}

		@Override
		@Valid
		@DependsOn(property = "enabled")
		@DependsOn(property = "mode", value = "write")
		public Child getChild() {
			return super.getChild();
		}

		@Override
		@Valid
		@DependsOn(property = "enabled")
		@DependsOn(property = "mode", value = "write")
		public List<Child> getChildren() {
			return super.getChildren();
		}
	}

	public static class ConditionalSettings {
		boolean enabled;

		public boolean isEnabled() {
			return enabled;
		}

		@NotNull
		@ShowCondition("isVisible")
		public String getName() {
			return null;
		}

		@Valid
		@ShowCondition("isVisible")
		public Child getChild() {
			return new Child();
		}

		@Valid
		@ShowCondition("isVisible")
		public List<Child> getChildren() {
			return List.of(new Child());
		}

		public static boolean isVisible() {
			return (boolean) EditContext.get().getInputValue("enabled");
		}
	}

	public static class DisplayNameCondition {
		boolean enabled;

		@Editable(name = "Enable Advanced Settings")
		public boolean isEnabled() {
			return enabled;
		}

		@NotNull
		@ShowCondition("isVisible")
		public String getName() {
			return null;
		}

		public static boolean isVisible() {
			return (boolean) EditContext.get().getInputValue("Enable Advanced Settings");
		}
	}

	public static class BrokenCondition {
		@NotNull
		@ShowCondition("isVisible")
		public String getName() {
			return null;
		}

		public static boolean isVisible() {
			throw new IllegalStateException("broken condition");
		}
	}
}
