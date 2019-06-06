package io.onedev.server.ci.job;

import java.io.File;

import org.slf4j.Logger;

import io.onedev.server.model.Build;

public interface DependencyPopulator {

	void populate(Build dependency, File workspace, Logger logger);
	
}
