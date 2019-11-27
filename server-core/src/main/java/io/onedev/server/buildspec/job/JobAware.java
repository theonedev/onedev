package io.onedev.server.buildspec.job;

import javax.annotation.Nullable;

public interface JobAware {
	
	@Nullable
	Job getJob();
	
}
