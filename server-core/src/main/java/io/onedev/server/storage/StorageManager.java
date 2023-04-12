package io.onedev.server.storage;

import io.onedev.commons.utils.FileUtils;

import java.io.File;

public interface StorageManager {
	
	void initLfsDir(Long projectId);

	void initArtifactsDir(Long projectId, Long buildNumber);
	
}
