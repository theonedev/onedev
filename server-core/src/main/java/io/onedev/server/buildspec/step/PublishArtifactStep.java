package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.SafePath;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=1050, name="Publish Artifacts")
public class PublishArtifactStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String sourcePath;
	
	private String artifacts;
	
	@Editable(order=50, name="From Path", placeholder="Job workspace", description="Optionally specify path "
			+ "relative to <a href='$docRoot/pages/concepts.md#job-workspace'>job workspace</a> to publish "
			+ "artifacts from. Leave empty to use job workspace itself")
	@Interpolative(variableSuggester="suggestVariables")
	@SafePath
	@Override
	public String getSourcePath() {
		return sourcePath;
	}
	
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	
	@Editable(order=100, description="Specify files to publish as job artifacts relative to "
			+ "source path specified above. Use * or ? for pattern match")
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
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getArtifacts());
	}

	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger jobLogger) {
		LockUtils.write(build.getArtifactsLockKey(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File artifactsDir = build.getArtifactsDir();
				FileUtils.createDir(artifactsDir);
				FileUtils.copyDirectory(inputDir, artifactsDir);
				return null;
			}
			
		});
		return null;
	}

}
