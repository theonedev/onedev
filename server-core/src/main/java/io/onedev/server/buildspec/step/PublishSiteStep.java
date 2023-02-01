package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.validation.constraints.NotEmpty;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.SafePath;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ProjectChoice;

@Editable(order=1060, name="Publish Site", description="This step publishes specified files to be served as project web site. "
		+ "Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>")
public class PublishSiteStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String sourcePath;
	
	private String siteFiles;
	
	@Editable(order=10, name="Project", placeholder="Current project", description="Optionally specify the project to "
			+ "publish site files to. Leave empty to publish to current project")
	@ProjectChoice
	public String getProjectPath() {
		return projectPath;
	}
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	@Editable(order=50, name="From Directory", placeholder="Job workspace", description="Optionally specify path "
			+ "relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish "
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
	
	@Editable(order=100, description="Specify files under above directory to be published. "
			+ "Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be "
			+ "included in these files to be served as site start page")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	public String getArtifacts() {
		return siteFiles;
	}

	public void setArtifacts(String artifacts) {
		this.siteFiles = artifacts;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getArtifacts());
	}

	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger jobLogger) {
		JobContext jobContext = OneDev.getInstance(JobManager.class).getJobContext(build.getId());
		if (jobContext.getJobExecutor().isSitePublishEnabled()) {
			Project project;
			if (projectPath != null) {
				project = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
				if (project == null)
					throw new ExplicitException("Unable to find project: " + projectPath);
			} else {
				project = build.getProject();
			}
			LockUtils.write(project.getSiteLockName(), new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					File projectSiteDir = OneDev.getInstance(StorageManager.class).getProjectSiteDir(project.getId());
					FileUtils.cleanDir(projectSiteDir);
					FileUtils.copyDirectory(inputDir, projectSiteDir);
					return null;
				}

			});
			String serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
			jobLogger.log("Site published as " 
					+ StringUtils.stripEnd(serverUrl, "/") + "/" + project.getPath() + "/~site");
			return null;
		} else {
			throw new UnauthorizedException("Site publish is prohibited by current job executor");
		}
	}

}
