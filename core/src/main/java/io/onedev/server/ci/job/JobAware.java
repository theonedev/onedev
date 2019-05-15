package io.onedev.server.ci.job;

import javax.annotation.Nullable;

public interface JobAware {
	
	@Nullable
	Job getJob();
	
}
