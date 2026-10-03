package io.onedev.server.buildspec.step;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.data.migration.VersionedYamlDoc;

class BuildImageStepTest extends AppLoaderMocker {
	@Override
	protected void setup() {
		when(AppLoader.getInstance(ImplementationRegistry.class)).thenReturn(new ImplementationRegistry() {
			@Override
			public <T> java.util.Collection<Class<? extends T>> getImplementations(Class<T> type) {
				var implementations = new java.util.ArrayList<Class<? extends T>>();
				if (type.isAssignableFrom(BuildImageStep.class))
					implementations.add(BuildImageStep.class.asSubclass(type));
				return implementations;
			}
		});
	}

	@Override
	protected void teardown() { }

	private VersionedYamlDoc document(String section, String option) {
		return VersionedYamlDoc.fromYaml("""
				version: 54
				%s:
				- name: image
				  steps:
				  - type: BuildImageStep
				    name: build
				    moreOptions: %s
				""".formatted(section, option));
	}

	@Test
	void requiresAdministratorMigrationOfLegacyOptionsInJobsAndTemplates() throws Exception {
		for (var section : java.util.List.of("jobs", "stepTemplates")) {
			var error = assertThrows(RuntimeException.class,
					() -> document(section, "--secret id=x,source=/host/file").toBean(BuildSpec.class));
			var explicit = io.onedev.commons.utils.ExceptionUtils.find(error, ExplicitException.class);
			assertNotNull(explicit);
			assertTrue(explicit.getMessage().contains("Image Build Options"));
		}
	}

	@Test
	void migratesEmptyLegacyOptionsAndRemovesStepSetting() {
		for (var value : java.util.List.of("null", "''")) {
			BuildSpec spec = document("jobs", value).toBean(BuildSpec.class);
			assertInstanceOf(BuildImageStep.class, spec.getJobs().get(0).getSteps().get(0));
			assertFalse(VersionedYamlDoc.fromBean(spec).toYaml().contains("moreOptions"));
		}
		assertThrows(NoSuchMethodException.class, () -> BuildImageStep.class.getMethod("getMoreOptions"));
	}
}
