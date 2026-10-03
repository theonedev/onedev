package io.onedev.server.job;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.Project;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;

class DependencyDestinationTest {

	@TempDir
	Path temp;

	private final byte[] marker = "dependency artifact\n".getBytes(StandardCharsets.UTF_8);

	private DefaultJobService service;

	private JobContext context;

	private ProjectService projectService;

	private BuildDependence dependence;

	private Path staging;

	@BeforeEach
	void setup() throws Exception {
		staging = Files.createDirectory(temp.resolve("staging"));
		var artifacts = Files.createDirectory(temp.resolve("artifacts"));
		Files.write(artifacts.resolve("proof.txt"), marker);
		Files.write(Files.createDirectory(artifacts.resolve("nested")).resolve("proof.txt"), marker);
		var project = new Project();
		project.setId(1L);
		var dependency = mock(Build.class);
		when(dependency.getProject()).thenReturn(project);
		when(dependency.getArtifactsDir()).thenReturn(artifacts.toFile());
		when(dependency.getArtifactsLockName()).thenReturn("dependency-destination-test");
		var build = new Build();
		dependence = new BuildDependence();
		dependence.setDependency(dependency);
		dependence.setDependent(build);
		dependence.setArtifacts("**");
		build.getDependencies().add(dependence);
		var buildService = mock(BuildService.class);
		when(buildService.load(1L)).thenReturn(build);
		projectService = mock(ProjectService.class);
		when(projectService.getActiveServer(1L, true)).thenReturn("local");
		var clusterService = mock(ClusterService.class);
		when(clusterService.getLocalServerAddress()).thenReturn("local");
		context = mock(JobContext.class);
		when(context.getBuildId()).thenReturn(1L);
		service = new DefaultJobService();
		FieldUtils.writeField(service, "buildService", buildService, true);
		FieldUtils.writeField(service, "projectService", projectService, true);
		FieldUtils.writeField(service, "clusterService", clusterService, true);
	}

	@Test
	void rejectsTraversalBeforeCreatingDirectoriesOrOverwritingFiles() throws Exception {
		var outside = Files.createDirectory(temp.resolve("outside"));
		var existing = Files.writeString(outside.resolve("proof.txt"), "keep existing contents");
		var original = Files.readAllBytes(existing);
		for (var destination : List.of("../outside", "../new-directory", "child/../../outside",
				"..\\outside", "child/..\\..\\outside", "..", "child/..")) {
			dependence.setDestinationPath(destination);
			assertThrows(ExplicitException.class, () -> service.copyDependencies(context, staging.toFile()), destination);
		}
		assertArrayEquals(original, Files.readAllBytes(existing));
		assertFalse(Files.exists(outside.resolve("nested")));
		assertFalse(Files.exists(temp.resolve("new-directory")));
		assertFalse(Files.exists(staging.resolve("child")));
		verifyNoInteractions(projectService);
	}

	@Test
	void rejectsAbsoluteAndDriveRelativePathsBeforeCopying() {
		for (var destination : List.of(temp.resolve("absolute").toString(), "C:\\outside", "C:outside",
				"\\\\host\\share\\outside")) {
			dependence.setDestinationPath(destination);
			assertThrows(ExplicitException.class, () -> service.copyDependencies(context, staging.toFile()), destination);
		}
		verifyNoInteractions(projectService);
	}

	@Test
	void rejectsSymlinkedDestinationsIncludingDanglingParents() throws Exception {
		assumeTrue(Files.getFileStore(temp).supportsFileAttributeView("posix"));
		var outside = Files.createDirectory(temp.resolve("outside"));
		Files.createSymbolicLink(staging.resolve("link"), outside);
		Files.createSymbolicLink(staging.resolve("dangling"), temp.resolve("missing"));
		for (var destination : List.of("link", "link/new-directory", "link/./new-directory", "dangling/child")) {
			dependence.setDestinationPath(destination);
			assertThrows(ExplicitException.class, () -> service.copyDependencies(context, staging.toFile()), destination);
		}
		assertFalse(Files.exists(outside.resolve("proof.txt")));
		assertFalse(Files.exists(outside.resolve("new-directory")));
		assertFalse(Files.exists(temp.resolve("missing")));
		verifyNoInteractions(projectService);
	}

	@Test
	void copiesArtifactsToDefaultAndContainedDestinations() throws Exception {
		for (var destination : Arrays.asList(null, "", ".", "nested/release..files")) {
			var input = Files.createTempDirectory(staging, "input-");
			dependence.setDestinationPath(destination);
			service.copyDependencies(context, input.toFile());
			var target = destination != null ? input.resolve(destination) : input;
			assertArrayEquals(marker, Files.readAllBytes(target.resolve("proof.txt")));
			assertArrayEquals(marker, Files.readAllBytes(target.resolve("nested/proof.txt")));
		}
	}
}
