package io.onedev.server.ci.job.outcome;

import java.io.File;

import io.onedev.server.model.Build2;

public interface DependencyPopulator {

	void populate(Build2 dependency, File workspace);
	
}
