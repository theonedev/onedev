package io.onedev.server.model.support.jobexecutor;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300)
public class KubernetesExecutor extends JobExecutor {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(String environment, File workspace, Map<String, String> envVars, 
			List<String> commands, SourceSnapshot snapshot, Collection<JobCache> caches, 
			PatternSet collectFiles, Logger logger) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkCaches() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cleanDir(File dir) {
		throw new UnsupportedOperationException();
	}

}