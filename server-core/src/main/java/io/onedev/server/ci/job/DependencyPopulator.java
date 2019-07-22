package io.onedev.server.ci.job;

import java.io.File;

import io.onedev.server.model.Build;
import io.onedev.server.util.JobLogger;

public interface DependencyPopulator {

	void populate(Build dependency, File workspace, JobLogger logger);
	
}
