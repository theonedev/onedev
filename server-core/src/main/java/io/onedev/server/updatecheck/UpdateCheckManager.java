package io.onedev.server.updatecheck;

import javax.annotation.Nullable;

public interface UpdateCheckManager {

	@Nullable
	String getNewVersionStatus();

	void cacheNewVersionStatus(String newVersionStatus);
	
	void clearCache();
	
}
