package io.onedev.server.data.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorkspaceProvisionModeMigrationTest {

	@TempDir
	Path directory;

	@Test
	void preservesContainerAndHostModesAndExposedPorts() {
		var file = directory.resolve("Projects.xml").toFile();
		var doc = new VersionedXmlDoc();
		var projects = doc.addElement("list");
		projects.addElement("io.onedev.server.model.Project").addElement("workspaceSpecs");
		var specs = projects.addElement("io.onedev.server.model.Project").addElement("workspaceSpecs");
		for (var mode : List.of("true", "false")) {
			var spec = specs.addElement("io.onedev.server.model.support.workspace.spec.WorkspaceSpec");
			spec.addElement("runInContainer").setText(mode);
			spec.addElement("containerPorts").addElement("int").setText("8080");
		}
		doc.writeToFile(file, false);

		assertTrue(MigrationHelper.migrate("252", new DataMigrator(), directory.toFile()));

		var migrated = VersionedXmlDoc.fromFile(file).getRootElement().elements().get(1)
				.element("workspaceSpecs").elements();
		assertEquals(List.of("DOCKER", "HOST"),
				migrated.stream().map(it -> it.elementText("provisionMode")).toList());
		for (var spec : migrated) {
			assertNull(spec.element("runInContainer"));
			assertNull(spec.element("containerPorts"));
			assertEquals("8080", spec.element("exposedPorts").elementText("int"));
		}
	}

	@Test
	void migratesHostExecutorAndProvisionerTypesWithoutChangingConfiguration() {
		var oldClasses = List.of(
				"io.onedev.server.plugin.executor.servershell.ServerShellExecutor",
				"io.onedev.server.plugin.executor.remoteshell.RemoteShellExecutor",
				"io.onedev.server.plugin.provisioner.servershell.ServerShellProvisioner",
				"io.onedev.server.ee.provisioner.RemoteShellProvisioner");
		var newClasses = List.of(
				"io.onedev.server.plugin.executor.serverhost.ServerHostExecutor",
				"io.onedev.server.plugin.executor.remotehost.RemoteHostExecutor",
				"io.onedev.server.plugin.provisioner.serverhost.ServerHostProvisioner",
				"io.onedev.server.ee.provisioner.RemoteHostProvisioner");
		var dockerClass = "io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor";
		var fileNames = List.of("Settings.xml", "Settings.xml.1");
		for (var fileName : fileNames) {
			var doc = new VersionedXmlDoc();
			var settings = doc.addElement("list");
			for (var key : List.of("JOB_EXECUTORS", "WORKSPACE_PROVISIONERS")) {
				var setting = settings.addElement("io.onedev.server.model.Setting");
				setting.addElement("key").setText(key);
				var value = setting.addElement("value").addAttribute("class", "list");
				var offset = key.equals("JOB_EXECUTORS") ? 0 : 2;
				for (var oldClass : oldClasses.subList(offset, offset + 2)) {
					var entry = value.addElement(oldClass).addAttribute("class", oldClass);
					entry.addElement("name").setText("custom-shell-name");
					entry.addElement("concurrency").setText("2");
					value.addElement(oldClass).addAttribute("reference", "../" + oldClass);
				}
			}
			settings.elements().get(0).element("value").addElement(dockerClass);
			doc.writeToFile(directory.resolve(fileName).toFile(), false);
		}

		assertTrue(MigrationHelper.migrate("252", new DataMigrator(), directory.toFile()));

		for (var fileName : fileNames) {
			var settings = VersionedXmlDoc.fromFile(directory.resolve(fileName).toFile()).getRootElement().elements();
			for (int i = 0; i < newClasses.size(); i++) {
				var entries = settings.get(i / 2).element("value").elements();
				var entry = entries.get(i % 2 * 2);
				assertEquals(newClasses.get(i), entry.getName());
				assertEquals(newClasses.get(i), entry.attributeValue("class"));
				assertEquals("custom-shell-name", entry.elementText("name"));
				assertEquals("2", entry.elementText("concurrency"));
				var reference = entries.get(i % 2 * 2 + 1);
				assertEquals(newClasses.get(i), reference.getName());
				assertEquals("../" + newClasses.get(i), reference.attributeValue("reference"));
			}
			assertEquals(dockerClass, settings.get(0).element("value").elements().get(4).getName());
		}
	}

}
