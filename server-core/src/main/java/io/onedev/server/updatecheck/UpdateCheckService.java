package io.onedev.server.updatecheck;

import org.jspecify.annotations.Nullable;

public interface UpdateCheckService {

	@Nullable
	String getNewVersionStatus();

	void cacheNewVersionStatus(String newVersionStatus);
	
	void clearCache();
	
}
