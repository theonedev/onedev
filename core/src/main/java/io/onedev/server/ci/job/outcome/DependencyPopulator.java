package io.onedev.server.ci.job.outcome;

import java.io.File;

import org.slf4j.Logger;

import io.onedev.server.model.Build2;

public interface DependencyPopulator {

	void populate(Build2 dependency, File workspace, Logger logger);
	
}
