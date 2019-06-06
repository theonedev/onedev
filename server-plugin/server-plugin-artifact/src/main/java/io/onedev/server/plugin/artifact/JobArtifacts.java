package io.onedev.server.plugin.artifact;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Artifacts")
public class JobArtifacts extends JobOutcome {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "artifacts";
	
	@Override
	public void process(Build build, File workspace, Logger logger) {
		File outcomeDir = getOutcomeDir(build, DIR);
		FileUtils.createDir(outcomeDir);

		LockUtils.write(getLockKey(build, DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				int baseLen = workspace.getAbsolutePath().length() + 1;
				for (File file: getPatternSet().listFiles(workspace)) {
					try {
						FileUtils.copyFile(file, new File(outcomeDir, file.getAbsolutePath().substring(baseLen)));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				return null;
			}
			
		});
	}

}
