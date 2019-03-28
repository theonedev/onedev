package io.onedev.server.ci.jobexecutor;

import javax.annotation.Nullable;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Build2;

@ExtensionPoint
public interface JobExecutorProvider {

	int DEFAULT_PRIORITY = 100;
	
	@Nullable
	JobExecutor getExecutor(Build2 build);
	
	int getPriority();
	
}
