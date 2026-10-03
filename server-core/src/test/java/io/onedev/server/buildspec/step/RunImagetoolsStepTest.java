package io.onedev.server.buildspec.step;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.data.migration.MigrationHelper;
import io.onedev.server.data.migration.VersionedYamlDoc;
import io.onedev.server.model.support.administration.DockerAware;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.KubernetesAware;

class RunImagetoolsStepTest extends AppLoaderMocker {

	@Override
	protected void setup() {
		when(AppLoader.getInstance(ImplementationRegistry.class)).thenReturn(new ImplementationRegistry() {
			@Override
			public <T> Collection<Class<? extends T>> getImplementations(Class<T> type) {
				var implementations = new ArrayList<Class<? extends T>>();
				if (type.isAssignableFrom(RunImagetoolsStep.class))
					implementations.add(RunImagetoolsStep.class.asSubclass(type));
				return implementations;
			}
		});
	}

	@Override
	protected void teardown() {
	}

	@Test
	void usesContainerCommandForLocalOciAndFileOptions() {
		var step = new RunImagetoolsStep();
		step.setArguments("create -f 'descriptor file.json' --metadata-file metadata.json "
				+ "-t oci-layout://combined:latest oci-layout://amd64:latest oci-layout://arm64:latest");
		// Inherited command settings cannot turn this into host execution.
		step.setRunInContainer(false);
		step.setImage("another-image");
		var facade = assertInstanceOf(CommandFacade.class, step.getFacade(null, null, "token", null));
		assertEquals("1dev/buildx:1.0.0", facade.getImage());
		assertEquals("sh", facade.getExecutable());
		assertEquals("docker buildx imagetools " + step.getArguments(), facade.getCommands());
		assertTrue(step.isApplicable(null, mock(JobExecutor.class, withSettings().extraInterfaces(DockerAware.class))));
		assertTrue(step.isApplicable(null, mock(JobExecutor.class, withSettings().extraInterfaces(KubernetesAware.class))));
		assertFalse(step.isApplicable(null, mock(JobExecutor.class)));
	}

	@Test
	void sharesRegistryOverridesAndTrustCertificatesWithCrane() throws Exception {
		var executor = mock(JobExecutor.class, withSettings().extraInterfaces(DockerAware.class));
		when(((DockerAware) executor).getRegistryLogins("token")).thenReturn(List.of(
				new RegistryLoginFacade("registry.example", "executor", "executor-password"),
				new RegistryLoginFacade("other.example", "other", "other-password")));
		var logins = List.of(new RegistryLoginFacade("registry.example", "job", "$literal-password"));
		var imagetools = new RunImagetoolsStep();
		imagetools.setArguments("inspect oci-layout://image:latest");
		var pull = new PullImageStep();
		pull.setSrcImage("registry.example/image:latest");
		pull.setDestPath("image");
		var push = new PushImageStep();
		push.setSrcPath("image");
		push.setDestImage("registry.example/image:latest");
		for (var step : List.of(imagetools, pull, push)) {
			step.setTrustCertificates("-----BEGIN CERTIFICATE-----\r\ncertificate\r\n-----END CERTIFICATE-----");
			var facade = (CommandFacade) step.getFacade(null, executor, "token", null, logins, Map.of("EXAMPLE", "value"));
			var commands = facade.getCommands();
			var configPrefix = "cat <<'EOF' > /root/.docker/config.json\n";
			var configStart = commands.indexOf(configPrefix) + configPrefix.length();
			assertTrue(commands.contains(configPrefix));
			var auths = new ObjectMapper().readTree(commands.substring(configStart, commands.indexOf("\nEOF", configStart))).get("auths");
			assertEquals(2, auths.size());
			assertEquals("job:$literal-password", new String(Base64.getDecoder().decode(auths.get("registry.example").get("auth").asText()), UTF_8));
			assertEquals("other:other-password", new String(Base64.getDecoder().decode(auths.get("other.example").get("auth").asText()), UTF_8));
			assertTrue(commands.contains("cat <<'EOF' > /root/trust-certs.crt\n-----BEGIN CERTIFICATE-----\ncertificate\n-----END CERTIFICATE-----\nEOF\nexport SSL_CERT_FILE=/root/trust-certs.crt\n"));
			assertTrue(commands.endsWith(step.getCommand()));
			assertEquals(logins, facade.getRegistryLogins());
			assertEquals(Map.of("EXAMPLE", "value"), facade.getEnvMap());
			assertEquals(step instanceof CraneStep ? "1dev/crane:1.0.0" : "1dev/buildx:1.0.0", facade.getImage());
		}
	}

	@Test
	void preservesExistingBuildSpecArgumentsAndRegistryLogins() {
		var yaml = """
				version: %s
				jobs:
				- name: image
				  steps:
				  - type: RunImagetoolsStep
				    name: manifest
				    arguments: create -t registry.example/image:latest registry.example/image:amd64
				    registryLogins:
				    - registryUrl: registry.example
				      userName: job
				      passwordSecret: registry-password
				""".formatted(MigrationHelper.getVersion(BuildSpec.class));
		BuildSpec spec = VersionedYamlDoc.fromYaml(yaml).toBean(BuildSpec.class);
		BuildSpec restored = VersionedYamlDoc.fromYaml(VersionedYamlDoc.fromBean(spec).toYaml()).toBean(BuildSpec.class);
		var step = assertInstanceOf(RunImagetoolsStep.class, restored.getJobs().get(0).getSteps().get(0));
		assertEquals("create -t registry.example/image:latest registry.example/image:amd64", step.getArguments());
		assertEquals("registry.example", step.getRegistryLogins().get(0).getRegistryUrl());
		assertEquals("job", step.getRegistryLogins().get(0).getUserName());
		assertEquals("registry-password", step.getRegistryLogins().get(0).getPasswordSecret());
		assertTrue(step.isRunInContainer());
		assertEquals("1dev/buildx:1.0.0", step.getImage());
	}
}
