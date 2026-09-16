package io.onedev.server.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.Test;

import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.FileProtection;

public class HibernateValidationContainerCascadeTest extends HibernateValidationTestSupport {

	@Test
	public void fileProtectionsRetainPropertyAndClassValidation() {
		var branch = new BranchProtection();
		branch.setBranches("main");
		var missingPaths = new FileProtection();
		missingPaths.setJobNames(List.of("build"));
		var missingRequirement = new FileProtection();
		missingRequirement.setPaths("src/**");
		branch.setFileProtections(List.of(missingPaths, missingRequirement));
		assertPaths(validator.validate(branch), "fileProtections[0].paths");

		missingPaths.setPaths("docs/**");
		assertPaths(validator.validate(branch), "fileProtections[1]");
		missingRequirement.setJobNames(List.of("test"));
		assertPaths(validator.validate(branch));

		var property = validator.getConstraintsForClass(BranchProtection.class)
				.getConstraintsForProperty("fileProtections");
		assertFalse(property.isCascaded());
		assertTrue(property.getConstrainedContainerElementTypes().iterator().next().isCascaded());
	}

	@Test
	public void containerCascadesRespectGetterOverrides() {
		assertPaths(validator.validate(new Parent()), "children[0].name");
		assertPaths(validator.validate(new Inherited()), "children[0].name");
		assertPaths(validator.validate(new Redeclared()), "children[0].name");
		assertPaths(validator.validate(new Removed()));
	}

	@Test
	public void nestedContainerElementsAreValidated() {
		assertPaths(validator.validate(new Nested()), "children[key].<map value>[0].name");
	}

	public static class Child {
		@NotNull
		public String getName() {
			return null;
		}
	}

	public static class Parent {
		private final List<Child> children = List.of(new Child());

		public List<@Valid Child> getChildren() {
			return children;
		}
	}

	public static class Inherited extends Parent {
	}

	public static class Redeclared extends Parent {
		@Override
		public List<@Valid Child> getChildren() {
			return super.getChildren();
		}
	}

	public static class Removed extends Parent {
		@Override
		public List<Child> getChildren() {
			return super.getChildren();
		}
	}

	public static class Nested {
		public Map<String, List<@Valid Child>> getChildren() {
			return Map.of("key", List.of(new Child()));
		}
	}
}
