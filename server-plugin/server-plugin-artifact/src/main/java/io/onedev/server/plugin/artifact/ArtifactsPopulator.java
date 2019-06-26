package io.onedev.server.plugin.artifact;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.job.DependencyPopulator;
import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.ci.job.log.JobLogger;
import io.onedev.server.model.Build;

public class ArtifactsPopulator implements DependencyPopulator {

	@Override
	public void populate(Build dependency, File workspace, JobLogger logger) {
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
