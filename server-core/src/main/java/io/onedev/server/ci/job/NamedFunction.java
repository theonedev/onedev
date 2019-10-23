package io.onedev.server.ci.job;

import javax.annotation.Nullable;

import io.onedev.server.model.Build;

public interface NamedFunction {

	String getName();
	
	@Nullable
	String call(Build build);
	
}
