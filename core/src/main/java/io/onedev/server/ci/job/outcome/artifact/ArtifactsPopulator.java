package io.onedev.server.ci.job.outcome.artifact;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.job.outcome.DependencyPopulator;
import io.onedev.server.ci.job.outcome.JobOutcome;
import io.onedev.server.model.Build;

public class ArtifactsPopulator implements DependencyPopulator {

	@Override
	public void populate(Build dependency, File workspace, Logger logger) {
		File outcomeDir = JobOutcome.getOutcomeDir(dependency, JobArtifacts.DIR);
		LockUtils.read(JobOutcome.getLockKey(dependency, JobArtifacts.DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				if (outcomeDir.exists()) 
					FileUtils.copyDirectory(outcomeDir, workspace);
				return null;
			}
			
		});
	}

}
