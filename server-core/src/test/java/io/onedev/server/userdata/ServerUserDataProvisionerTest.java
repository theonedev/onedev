package io.onedev.server.userdata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.UserDataEntryFacade;
import io.onedev.k8shelper.UserDataFacade;
import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
import io.onedev.server.util.PathIndexUtils;

@EnabledOnOs({OS.LINUX, OS.MAC})
class ServerUserDataProvisionerTest {

	@TempDir
	Path temp;

	@Test
	void initializesFilesDirectoriesAndMissingPathsWithoutReplacingLocalEdits() throws Exception {
		var sourceFile = Files.writeString(temp.resolve("image's file"), "image file");
		var sourceDir = Files.createDirectory(temp.resolve("image directory"));
		Files.writeString(sourceDir.resolve("contents"), "image directory");
		var missing = temp.resolve("missing");
		var workspace = Files.createDirectory(temp.resolve("workspace"));
		var service = mock(UserService.class);
		when(service.getWorkspaceDataDir(1L, "key", false)).thenReturn(temp.resolve("stored").toFile());
		var provisioner = new ServerUserDataProvisioner(1L, List.of(new UserDataFacade("key", List.of(
				new UserDataEntryFacade(sourceFile.toString(), null),
				new UserDataEntryFacade(sourceDir.toString(), null),
				new UserDataEntryFacade(missing.toString(), null)))));
		try (var oneDev = mockStatic(OneDev.class)) {
			oneDev.when(() -> OneDev.getInstance(UserService.class)).thenReturn(service);
			var script = provisioner.getInitEntrypointArgs(workspace.toString());
			runScript(script);
			var dataFile = provisioner.getPathFile(workspace.toFile(), "key", sourceFile.toString()).toPath();
			var dataDir = provisioner.getPathFile(workspace.toFile(), "key", sourceDir.toString()).toPath();
			var emptyDir = provisioner.getPathFile(workspace.toFile(), "key", missing.toString()).toPath();
			var state = workspace.resolve(provisioner.getStatePath("key", sourceFile.toString()));
			var baseline = Files.getLastModifiedTime(state);
			assertEquals("image file", Files.readString(dataFile));
			assertEquals("image directory", Files.readString(dataDir.resolve("contents")));
			assertTrue(Files.isDirectory(emptyDir));
			var logger = mock(TaskLogger.class);
			provisioner.download(workspace.toFile(), logger);
			provisioner.upload(workspace.toFile(), logger);
			verify(service, never()).uploadWorkspaceData(anyLong(), anyString(), anyString(), any());
			Files.writeString(dataFile, "local file edits");
			Files.writeString(dataDir.resolve("contents"), "local directory edits");
			runScript(script);
			assertEquals("local file edits", Files.readString(dataFile));
			assertEquals("local directory edits", Files.readString(dataDir.resolve("contents")));
			assertEquals(baseline, Files.getLastModifiedTime(state));
		}
	}

	@Test
	void storedUserDataDoesNotInitializeFromImage() throws Exception {
		var stored = Files.createDirectory(temp.resolve("stored"));
		PathIndexUtils.write(stored.toFile(), Map.of("/home/user/data", 1));
		Files.writeString(stored.resolve("1"), "stored archive");
		var service = mock(UserService.class);
		when(service.getWorkspaceDataDir(1L, "key", false)).thenReturn(stored.toFile());
		var provisioner = new ServerUserDataProvisioner(1L, List.of(new UserDataFacade("key",
				List.of(new UserDataEntryFacade("/home/user/data", null)))));
		try (var oneDev = mockStatic(OneDev.class)) {
			oneDev.when(() -> OneDev.getInstance(UserService.class)).thenReturn(service);
			assertNull(provisioner.getInitEntrypointArgs(temp.resolve("workspace").toString()));
		}
	}

	private void runScript(String script) throws Exception {
		var process = new ProcessBuilder("sh", "-c", script).redirectErrorStream(true).start();
		var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertEquals(0, process.waitFor(), output);
	}

}
