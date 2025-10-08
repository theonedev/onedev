package io.onedev.server.service;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;

public interface BuildParamService extends EntityService<BuildParam> {
	
	void create(BuildParam param);
	
	Collection<String> getParamNames(@Nullable Project project);

}
