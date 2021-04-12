package io.onedev.server.buildspec;

import java.util.List;

import io.onedev.server.buildspec.param.spec.ParamSpec;

public interface ParamSpecAware {

	List<ParamSpec> getParamSpecs();
	
}
