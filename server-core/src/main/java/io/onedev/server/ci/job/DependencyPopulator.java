package io.onedev.server.ci.job;

import java.io.File;

import io.onedev.server.ci.job.log.JobLogger;
import io.onedev.server.model.Build;

public interface DependencyPopulator {

	void populate(Build dependency, File workspace, JobLogger logger);
	
}
