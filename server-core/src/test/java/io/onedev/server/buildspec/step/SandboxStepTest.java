package io.onedev.server.buildspec.step;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.onedev.server.model.support.administration.DockerAware;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.KubernetesAware;
import io.onedev.server.model.support.administration.jobexecutor.SandboxAware;
import io.onedev.server.validation.HibernateValidationTestSupport;

class SandboxStepTest extends HibernateValidationTestSupport {

	@Test
	void sandboxAcceptsShellAndDockerStepsWithoutEnablingOtherExecutors() {
		var sandbox = mock(JobExecutor.class, withSettings().extraInterfaces(SandboxAware.class));
		var docker = mock(JobExecutor.class, withSettings().extraInterfaces(DockerAware.class));
		var kubernetes = mock(JobExecutor.class, withSettings().extraInterfaces(KubernetesAware.class));
		var shell = mock(JobExecutor.class);
		var command = new CommandStep();
		for (boolean container : List.of(true, false)) {
			command.setRunInContainer(container);
			assertTrue(command.isApplicable(null, sandbox));
			assertEquals(container, command.isApplicable(null, docker));
			assertEquals(container, command.isApplicable(null, kubernetes));
			assertEquals(!container, command.isApplicable(null, shell));
		}
		for (var step : List.of(new RunContainerStep(), new BuildImageStep(), new RunImagetoolsStep(), new PruneBuilderCacheStep())) {
			assertTrue(step.isApplicable(null, sandbox));
			assertTrue(step.isApplicable(null, docker));
			assertFalse(step.isApplicable(null, shell));
			assertFalse(step.isApplicable(null, kubernetes));
		}
	}

	@Test
	void buildOptionsAllowFileAccessButRejectSettingsOverrides() {
		var step = new BuildImageStep();
		step.setMoreOptions("--secret id=key,src=/tmp/key --metadata-file /tmp/metadata --cache-to type=local,dest=/tmp/cache");
		assertTrue(validator.validateProperty(step, "moreOptions").isEmpty());
		for (var option : List.of("--builder=other", "--platform=linux/arm64", "--push=false", "--load",
				"-fOther", "--file Other", "-tother", "--tag=other", "-qotmp", "--output=type=local", "--")) {
			step.setMoreOptions(option);
			assertFalse(validator.validateProperty(step, "moreOptions").isEmpty(), option);
		}
	}
}
