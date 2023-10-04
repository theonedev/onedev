package io.onedev.server.imports;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.util.ImportStep;

import java.io.Serializable;
import java.util.List;

public interface ProjectImporter extends Serializable {

	String getName();
	
	List<ImportStep<? extends Serializable>> getSteps();

	TaskResult doImport(boolean dryRun, TaskLogger logger);
	
}
