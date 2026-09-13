package io.onedev.server.validation;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.junit.Test;

public class HibernateValidationInheritanceTest extends HibernateValidationTestSupport {

	@Test
	public void childGetterReplacesConstraintAttributes() {
		// MetaDataBuilder: editable subclasses can redefine a constraint of the same type.
		var descriptors = validator.getConstraintsForClass(ChildSize.class)
				.getConstraintsForProperty("name").getConstraintDescriptors();
		assertEquals(1, descriptors.size());
		assertEquals(2, ((Size) descriptors.iterator().next().getAnnotation()).min());
		assertPaths(validator.validateValue(ChildSize.class, "name", "abc"));
		var violations = validator.validateValue(ChildSize.class, "name", "a");
		assertPaths(violations, "name");
		assertEquals("child size", violations.iterator().next().getMessage());
		assertPaths(validator.validateProperty(new ParentSize(), "name"), "name");
	}

	@Test
	public void defaultGroupRetainsParentConstraintsOfTheSameType() {
		// Unlike validateValue and explicit groups, default-group bean/property validation
		// also checks the parent's annotation when the child redeclares the same type.
		var bean = new ChildSize();
		for (Class<?> group : new Class<?>[] {Default.class, Checks.class}) {
			bean.name = "abc";
			for (var violations : List.of(validator.validate(bean, group),
					validator.validateProperty(bean, "name", group))) {
				if (group == Default.class) {
					assertPaths(violations, "name");
					assertEquals("parent size", violations.iterator().next().getMessage());
				} else {
					assertPaths(violations);
				}
			}
			bean.name = "a";
			for (var violations : List.of(validator.validate(bean, group),
					validator.validateProperty(bean, "name", group))) {
				assertPaths(violations, group == Default.class ? new String[] {"name", "name"} : new String[] {"name"});
				assertEquals(group == Default.class ? Set.of("parent size", "child size") : Set.of("child size"), violations.stream()
						.map(it -> it.getMessage()).collect(Collectors.toSet()));
			}
		}
	}

	@Test
	public void overridingGetterCanRemoveConstraintsWhileInheritedGettersRetainThem() {
		// ValidatorImpl: derived build steps compute properties that were required user inputs.
		assertPaths(validator.validate(new RequiredName()), "name");
		assertPaths(validator.validate(new InheritedName()), "name");
		assertPaths(validator.validate(new OptionalName()));
		assertPaths(validator.validateProperty(new OptionalName(), "name"));
	}

	@Test
	public void overridingGetterCanRemoveObjectAndCollectionCascades() {
		assertPaths(validator.validate(new ParentCascade()), "child.name", "children[0].name");
		assertPaths(validator.validate(new InheritedCascade()), "child.name", "children[0].name");
		assertPaths(validator.validate(new RemovedCascade()));
	}

	@Test
	public void overridingGetterCanRedeclareValidWithoutMetadataErrors() {
		// MethodValidationConfiguration, OD-2564: @Valid on both hierarchy levels is legal
		// for OneDev's replacement semantics; it must still validate the returned children.
		assertPaths(validator.validate(new RedeclaredCascade()), "child.name", "children[0].name");
	}

	public interface Checks {
	}

	public static class ParentSize {
		String name = "abc";

		@Size(min = 5, message = "parent size", groups = {Default.class, Checks.class})
		public String getName() {
			return name;
		}
	}

	public static class ChildSize extends ParentSize {
		@Override
		@Size(min = 2, message = "child size", groups = {Default.class, Checks.class})
		public String getName() {
			return super.getName();
		}
	}

	public static class RequiredName {
		@NotNull
		public String getName() {
			return null;
		}
	}

	public static class InheritedName extends RequiredName {
	}

	public static class OptionalName extends RequiredName {
		@Override
		public String getName() {
			return null;
		}
	}

	public static class ParentCascade {
		private final RequiredName child = new RequiredName();
		private final List<RequiredName> children = List.of(new RequiredName());

		@Valid
		public RequiredName getChild() {
			return child;
		}

		@Valid
		public List<RequiredName> getChildren() {
			return children;
		}
	}

	public static class InheritedCascade extends ParentCascade {
	}

	public static class RemovedCascade extends ParentCascade {
		@Override
		public RequiredName getChild() {
			return super.getChild();
		}

		@Override
		public List<RequiredName> getChildren() {
			return super.getChildren();
		}
	}

	public static class RedeclaredCascade extends ParentCascade {
		@Override
		@Valid
		public RequiredName getChild() {
			return super.getChild();
		}

		@Override
		@Valid
		public List<RequiredName> getChildren() {
			return super.getChildren();
		}
	}
}
