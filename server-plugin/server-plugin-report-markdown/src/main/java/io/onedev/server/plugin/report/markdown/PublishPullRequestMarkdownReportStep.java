package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=120, name="Publish Markdown Report (for Pull Request)", description="This report will be displayed in pull request overview "
		+ "page if the build belongs to a pull request")
public class PublishPullRequestMarkdownReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "pull-request-markdown";
	
	public static final String FILE = "content.md";

	private String file;
	
	@Editable(order=1100, description="Specify markdown file relative to repository workspace to publish")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public String getFilePatterns() {
		return getFile();
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	@Override
	public Map<String, byte[]> run(Build build, File workspace, TaskLogger logger) {
		if (build.getRequest() != null) {
			LockUtils.write(getReportLockKey(build), new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					File file = new File(workspace, getFile()); 
					if (file.exists()) {
						File reportDir = new File(build.getPublishDir(), CATEGORY + "/" + getReportName());
						String markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
						FileUtils.createDir(reportDir);
						FileUtils.writeFile(new File(reportDir, FILE), markdown, StandardCharsets.UTF_8.name());
					} else {
						logger.log("WARNING: Markdown report file not found: " + file.getAbsolutePath());
					}
					return null;
				}
				
			});
		}
		return null;
	}

	public static String getReportLockKey(Build build) {
		return PublishPullRequestMarkdownReportStep.class.getName() + ":" + build.getId();
	}

}
