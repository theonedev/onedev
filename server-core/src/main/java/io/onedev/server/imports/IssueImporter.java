package io.onedev.server.imports;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.model.Project;
import io.onedev.server.web.util.ImportStep;

public interface IssueImporter extends Serializable {

	String getName();

	List<ImportStep<? extends Serializable>> getSteps();
	
	@Nullable
	public abstract String doImport(Project project, boolean retainIssueNumbers, boolean dryRun, TaskLogger logger);
	
}
