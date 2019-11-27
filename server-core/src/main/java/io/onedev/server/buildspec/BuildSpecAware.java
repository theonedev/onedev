package io.onedev.server.buildspec;

import javax.annotation.Nullable;

public interface BuildSpecAware {
	
	@Nullable
	BuildSpec getBuildSpec();
	
}
