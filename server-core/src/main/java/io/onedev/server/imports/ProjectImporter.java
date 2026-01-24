package io.onedev.server.imports;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.util.ImportStep;

public abstract class ProjectImporter implements Serializable {

	private String parentProjectPath;
	
	public abstract String getName();
	
	public abstract List<ImportStep<? extends Serializable>> getSteps();

	public abstract TaskResult doImport(boolean dryRun, TaskLogger logger);
	
	public void setParentProjectPath(@Nullable String parentProjectPath) {
		this.parentProjectPath = parentProjectPath;
	}

	@Nullable
	public String getParentProjectPath() {
		return parentProjectPath;
	}

}
