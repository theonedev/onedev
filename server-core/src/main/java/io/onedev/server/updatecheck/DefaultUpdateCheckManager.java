package io.onedev.server.updatecheck;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class DefaultUpdateCheckManager implements UpdateCheckManager {

	private volatile UpdateCheck updateCheck;
	
	@Nullable
	public String getNewVersionStatus() {
		var updateCheck = this.updateCheck;
		if (updateCheck == null || updateCheck.isOutdated())
			return null;
		else
			return updateCheck.getNewVersionStatus();
	}

	public void cacheNewVersionStatus(String newVersionStatus) {
		if (newVersionStatus != null)
			updateCheck = new UpdateCheck(newVersionStatus, new Date());
		else
			updateCheck = null;
	}

	@Override
	public void clearCache() {
		updateCheck = null;
	}

}
