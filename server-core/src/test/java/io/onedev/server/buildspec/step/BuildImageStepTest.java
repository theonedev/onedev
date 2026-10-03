package io.onedev.server.buildspec.step;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.data.migration.VersionedYamlDoc;

class BuildImageStepTest extends AppLoaderMocker {
	@Override
	protected void setup() {
		when(AppLoader.getInstance(ImplementationRegistry.class)).thenReturn(new ImplementationRegistry() {
			@Override
			public <T> java.util.Collection<Class<? extends T>> getImplementations(Class<T> type) {
				var implementations = new java.util.ArrayList<Class<? extends T>>();
				for (var implementation : java.util.List.of(BuildImageStep.class, BuildImageStep.RegistryOutput.class)) {
					if (type.isAssignableFrom(implementation))
						implementations.add(implementation.asSubclass(type));
				}
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
				    output:
				      type: RegistryOutput
				      tags: test:latest
				    moreOptions: %s
				""".formatted(section, option));
	}

	@Test
	void preservesStepOptionsInJobsAndTemplates() {
		for (var section : java.util.List.of("jobs", "stepTemplates")) {
			var options = "--no-cache --build-arg REVISION=@commit_hash@";
			BuildSpec spec = document(section, options).toBean(BuildSpec.class);
			BuildSpec restored = VersionedYamlDoc.fromYaml(VersionedYamlDoc.fromBean(spec).toYaml()).toBean(BuildSpec.class);
			var steps = section.equals("jobs") ? restored.getJobs().get(0).getSteps()
					: restored.getStepTemplates().get(0).getSteps();
			var step = assertInstanceOf(BuildImageStep.class, steps.get(0));
			assertEquals(options, step.getMoreOptions());
			var facade = assertInstanceOf(BuildImageFacade.class, step.getFacade(null, null, "token", null));
			assertEquals(options, facade.getMoreOptions());
		}
	}

	@Test
	void acceptsEmptyStepOptions() {
		for (var value : java.util.List.of("null", "''")) {
			BuildSpec spec = document("jobs", value).toBean(BuildSpec.class);
			var step = assertInstanceOf(BuildImageStep.class, spec.getJobs().get(0).getSteps().get(0));
			assertNull(step.getMoreOptions());
		}
	}
}
