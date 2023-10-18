package io.onedev.server.imports;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.util.ImportStep;

import java.io.Serializable;
import java.util.List;

public interface IssueImporter extends Serializable {

	String getName();

	List<ImportStep<? extends Serializable>> getSteps();
	
	public abstract TaskResult doImport(Long projectId, boolean dryRun, TaskLogger logger);
	
}
