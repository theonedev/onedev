package io.onedev.server;

import java.io.File;

public interface StorageManager {

	File initLfsDir(Long projectId);

	File initArtifactsDir(Long projectId, Long buildNumber);
	
	File initPacksDir(Long projectId);

}
