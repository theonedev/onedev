package io.onedev.server.validation;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;

class DependencyDestinationValidationTest extends HibernateValidationTestSupport {

	@Test
	void rejectsUnsafeDestinationsForBothDependencyTypes() {
		for (var type : List.of(JobDependency.class, ProjectDependency.class)) {
			for (var destination : List.of("../outside", "child/../../outside", "..\\outside",
					"/outside", "C:\\outside", "C:outside", "\\\\host\\share\\outside")) {
				assertPaths(validator.validateValue(type, "destinationPath", destination), "destinationPath");
			}
		}
	}

	@Test
	void allowsDefaultRelativeAndInterpolatedDestinations() {
		for (var type : List.of(JobDependency.class, ProjectDependency.class)) {
			for (var destination : Arrays.asList(null, "", ".", "nested/release..files", "@param:destination@"))
				assertPaths(validator.validateValue(type, "destinationPath", destination));
		}
	}
}
