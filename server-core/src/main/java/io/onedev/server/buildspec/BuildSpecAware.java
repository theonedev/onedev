package io.onedev.server.buildspec;

import org.jspecify.annotations.Nullable;

public interface BuildSpecAware {
	
	@Nullable
	BuildSpec getBuildSpec();
	
}
