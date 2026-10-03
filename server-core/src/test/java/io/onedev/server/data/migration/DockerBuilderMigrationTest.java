package io.onedev.server.data.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DockerBuilderMigrationTest {

	@TempDir
	Path directory;

	@Test
	void clearsDefaultAndCustomBuildersAcrossSettingsBatches() {
		var executorTypes = List.of(
				"io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor",
				"io.onedev.server.plugin.executor.remotedocker.RemoteDockerExecutor");
		for (int batch = 0; batch < executorTypes.size(); batch++) {
			var doc = new VersionedXmlDoc();
			var list = doc.addElement("list");
			var setting = list.addElement("io.onedev.server.model.Setting");
			setting.addElement("key").setText("JOB_EXECUTORS");
			var executors = setting.addElement("value");
			for (var builder : List.of("onedev", "custom-builder", "")) {
				var executor = executors.addElement(executorTypes.get(batch));
				executor.addElement("name").setText("docker-" + builder);
				if (!builder.isEmpty())
					executor.addElement("dockerBuilder").setText(builder);
				executor.addElement("runOptions").setText("--read-only");
			}
			var otherSetting = list.addElement("io.onedev.server.model.Setting");
			otherSetting.addElement("key").setText("OTHER");
			otherSetting.addElement("value").addElement("dockerBuilder").setText("preserved");
			var emptySetting = list.addElement("io.onedev.server.model.Setting");
			emptySetting.addElement("key").setText("JOB_EXECUTORS");
			doc.writeToFile(directory.resolve("Settings.xml" + (batch == 0 ? "" : "." + batch)).toFile(), false);
		}

		assertTrue(MigrationHelper.migrate("247", new DataMigrator(), directory.toFile()));

		for (int batch = 0; batch < executorTypes.size(); batch++) {
			var doc = VersionedXmlDoc.fromFile(
					directory.resolve("Settings.xml" + (batch == 0 ? "" : "." + batch)).toFile());
			var settings = doc.getRootElement().elements();
			var executors = settings.get(0).element("value").elements();
			assertEquals(3, executors.size());
			for (int i = 0; i < executors.size(); i++) {
				var executor = executors.get(i);
				assertEquals(executorTypes.get(batch), executor.getName());
				assertEquals(List.of("docker-onedev", "docker-custom-builder", "docker-").get(i),
						executor.elementText("name"));
				assertNull(executor.element("dockerBuilder"));
				assertEquals("--read-only", executor.elementText("runOptions"));
			}
			assertEquals("preserved", settings.get(1).element("value").elementText("dockerBuilder"));
			assertNull(settings.get(2).element("value"));
		}
	}
}
