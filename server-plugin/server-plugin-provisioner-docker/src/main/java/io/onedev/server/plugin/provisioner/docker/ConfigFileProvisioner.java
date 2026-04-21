package io.onedev.server.plugin.provisioner.docker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.annotation.Path;
import io.onedev.server.model.support.workspace.spec.ConfigFile;
import io.onedev.server.validation.validator.PathValidator;
import io.onedev.server.workspace.WorkspaceContext;

public class ConfigFileProvisioner {
	
	private final File workspaceDir;

	private final List<ConfigFile> configFiles;

	public ConfigFileProvisioner(File workspaceDir, WorkspaceContext context) {
		this.workspaceDir = workspaceDir;
		this.configFiles = context.getSpec().getConfigFiles();
	}

	public Map<String, File> setup() {
		var cacheIndex = 1;
		var pathMap = new HashMap<String, File>();
		for (var configFile : configFiles) {
			var path = configFile.getPath();
			String checkMessage = PathValidator.checkPath(Path.Type.ABSOLUTE, path);
			if (checkMessage != null)
				throw new ExplicitException("Invalid config file path '" + path + "': " + checkMessage);

			File pathFile = new File(workspaceDir, "config-files/" + (cacheIndex++));

			try {
				FileUtils.writeStringToFile(pathFile, configFile.getContent(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			pathMap.put(path, pathFile);
		}
		return pathMap;
	}

}
