package io.onedev.server.buildspec.job.projectdependency;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.annotation.Editable;

@Editable
public interface BuildProvider extends Serializable {

	@Nullable
	Build getBuild(Project project);
	
	String getDescription();
}
