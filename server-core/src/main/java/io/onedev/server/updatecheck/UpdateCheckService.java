package io.onedev.server.updatecheck;

import javax.annotation.Nullable;

public interface UpdateCheckService {

	@Nullable
	String getNewVersionStatus();

	void cacheNewVersionStatus(String newVersionStatus);
	
	void clearCache();
	
}
