package io.onedev.server.ci.job.outcome;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.model.Build2;

public class ArtifactsPopulator implements DependencyPopulator {

	@Override
	public void populate(Build2 dependency, File workspace) {
		File outcomeDir = JobOutcome.getOutcomeDir(dependency, JobArtifacts.NAME);
		LockUtils.read(JobOutcome.getLockKey(dependency, JobArtifacts.NAME), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				if (outcomeDir.exists()) 
					FileUtils.copyDirectory(outcomeDir, workspace);
				return null;
			}
			
		});
	}

}
