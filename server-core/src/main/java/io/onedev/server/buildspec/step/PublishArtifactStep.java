package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=105, name="Publish Artifacts")
public class PublishArtifactStep extends ServerStep {

	private static final long serialVersionUID = 1L;

	private String artifacts;
	
	@Editable(order=100, description="Specify files to publish as job artifacts relative to "
			+ "repository root. Use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith);
	}
	
	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getArtifacts());
	}

	@Override
	public void run(Build build, File filesDir, SimpleLogger jobLogger) {
		LockUtils.write(build.getArtifactsLockKey(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File artifactsDir = build.getArtifactsDir();
				FileUtils.createDir(artifactsDir);
				FileUtils.copyDirectory(filesDir, artifactsDir);
				return null;
			}
			
		});
	}

}
