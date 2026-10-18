package io.onedev.server.buildspec.step;

import static io.onedev.server.buildspec.step.StepGroup.PUBLISH;

import java.io.File;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Path;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.annotation.UrlSegment;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.patternset.PatternSet;

@Editable(order=1060, name="Site", group = PUBLISH, description="This step publishes specified files to be served as project web site. "
		+ "Sites are published at <code>/path/to/project/~sites/&lt;site name&gt;/</code>. "
		+ "Leave the site name empty to publish a public site at <code>/path/to/project/~sites/public/</code>")
public class PublishSiteStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String sourcePath;

	private String siteName;
	
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
	
	@Editable(order=20, placeholder="Public site", description="Optionally specify a site name. Named sites require "
			+ "access permission configured in roles. Leave empty to publish a publicly accessible site. "
			+ "The name 'public' is reserved (case insensitive)")
	@UrlSegment
	@Interpolative(variableSuggester = "suggestVariables")
	@ReservedOptions(value="(?i)public", message="Site name 'public' is reserved")
	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	@Editable(order=50, name="From Directory", placeholder="Job workdir", description="Optionally specify path "
			+ "relative to <a href='${docsite}/concepts#job-workdir'>job working directory</a> to publish "
			+ "artifacts from. Leave empty to use job working directory itself")
	@Interpolative(variableSuggester="suggestVariables")
	@Path(Path.Type.RELATIVE)
	@Override
	public String getSourcePath() {
		return sourcePath;
	}
	
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	
	@Editable(order=100, description="Specify files under above directory to be published. "
			+ "Use * or ? for pattern match. <b>NOTE:</b> If <code>index.html</code> is included, "
			+ "it will be served as the site start page; otherwise a listing of published files will be shown")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	public String getSiteFiles() {
		return siteFiles;
	}

	public void setSiteFiles(String siteFiles) {
		this.siteFiles = siteFiles;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getSiteFiles());
	}

	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return OneDev.getInstance(SessionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			JobContext jobContext = OneDev.getInstance(JobService.class).getJobContext(build.getId());
			if (jobContext.getJobExecutor().isSitePublishEnabled()) {
				Project project;
				if (projectPath != null) {
					project = OneDev.getInstance(ProjectService.class).findByPath(projectPath);
					if (project == null) {
						logger.error("Unable to find project: " + projectPath);
						return new ServerStepResult(false);
					}
				} else {
					project = build.getProject();
				}
				var projectId = project.getId();
				LockUtils.write(project.getSitesLockName(), () -> {
					File projectSitesDir = OneDev.getInstance(ProjectService.class).getSitesDir(projectId);
					var siteDir = new File(projectSitesDir,
							siteName != null ? siteName : Project.PUBLIC_SITE);
					FileUtils.cleanDir(siteDir);
					FileUtils.copyDirectory(inputDir, siteDir);
					OneDev.getInstance(ProjectService.class).directoryModified(projectId, projectSitesDir);
					return null;
				});
				String serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
				logger.log("Site published as "
						+ StringUtils.stripEnd(serverUrl, "/") + "/" + project.getPath() + "/~sites/"
						+ (siteName != null ? siteName : Project.PUBLIC_SITE) + "/");
			} else {
				logger.error("Site publish is prohibited by current job executor");
				return new ServerStepResult(false);
			}
			return new ServerStepResult(true);
		});
	}
	
}
