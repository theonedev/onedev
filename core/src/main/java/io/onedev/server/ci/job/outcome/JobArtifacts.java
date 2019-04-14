package io.onedev.server.ci.job.outcome;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.model.Build2;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Artifacts")
public class JobArtifacts extends JobOutcome {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "artifacts";
	
	@Override
	public void process(Build2 build, File workspace) {
		File outcomeDir = getOutcomeDir(build, NAME);
		FileUtils.createDir(outcomeDir);

		LockUtils.write(getLockKey(build, NAME), new Callable<Void>() {

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
