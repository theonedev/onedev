package io.onedev.server.imports;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.web.util.ImportStep;

public interface ProjectImporter extends Serializable {

	String getName();
	
	List<ImportStep<? extends Serializable>> getSteps();

	@Nullable
	String doImport(boolean dryRun, TaskLogger logger);
	
}
