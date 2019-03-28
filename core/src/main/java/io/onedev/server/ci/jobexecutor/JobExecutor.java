package io.onedev.server.ci.jobexecutor;

import javax.annotation.Nullable;

import io.onedev.server.model.Build2;

public interface JobExecutor {

	@Nullable
	String run(Build2 build);

	boolean isRunning(Build2 build);
	
	void stop(Build2 build);
	
}
