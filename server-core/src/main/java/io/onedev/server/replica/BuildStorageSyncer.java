package io.onedev.server.replica;

import io.onedev.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface BuildStorageSyncer {
	
	void sync(Long projectId, Long buildNumber, String activeServer);
	
}
