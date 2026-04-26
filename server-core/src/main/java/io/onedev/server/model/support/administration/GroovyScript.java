package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.JobMatch;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class GroovyScript implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String BUILTIN_PREFIX = "builtin:";

	private String name;
	
	private List<String> content;
	
	private boolean canBeUsedByBuildJobs = true;
	
	private String jobAuthorization;

	private boolean canBeUsedByWorkspaceSpecs = true;

	private String applicableWorkspaceProjects;

	@Editable(order=100)
	@Pattern(regexp ="^(?!" + BUILTIN_PREFIX + ").*$", message="Name is not allowed to start with '" + BUILTIN_PREFIX + "'")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=300)
	@Code(language = Code.GROOVY)
	@Size(min=1, message="May not be empty")
	public List<String> getContent() {
		return content;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}
	
	@Editable(order=350, name="Can Be Used By Jobs", description = "Whether or not this script can be used in CI/CD jobs")
	public boolean isCanBeUsedByBuildJobs() {
		return canBeUsedByBuildJobs;
	}

	public void setCanBeUsedByBuildJobs(boolean canBeUsedByBuildJobs) {
		this.canBeUsedByBuildJobs = canBeUsedByBuildJobs;
	}
	
	@Editable(order=500, placeholder="Any job", description="Optionally specify jobs allowed to use this script")
	@DependsOn(property="canBeUsedByBuildJobs")
	@JobMatch(withProjectCriteria = true)
	public String getJobAuthorization() {
		return jobAuthorization;
	}

	public void setJobAuthorization(String jobAuthorization) {
		this.jobAuthorization = jobAuthorization;
	}
	
	@Editable(order=550, name="Can Be Used By Workspace Specs", description = "Whether or not this script can be used in workspace spec")
	public boolean isCanBeUsedByWorkspaceSpecs() {
		return canBeUsedByWorkspaceSpecs;
	}

	public void setCanBeUsedByWorkspaceSpecs(boolean canBeUsedByWorkspaceSpecs) {
		this.canBeUsedByWorkspaceSpecs = canBeUsedByWorkspaceSpecs;
	}
	
	@Editable(order=600, placeholder="Any project", description="Optionally specify projects applicable for this provider. " +
			"Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. " +
			"Multiple projects should be separated by space")
	@Patterns(suggester="suggestProjects", path=true)
	@DependsOn(property="canBeUsedByWorkspaceSpecs")		
	public String getApplicableWorkspaceProjects() {
		return applicableWorkspaceProjects;
	}

	public void setApplicableWorkspaceProjects(String applicableWorkspaceProjects) {
		this.applicableWorkspaceProjects = applicableWorkspaceProjects;
	}
	
	public final boolean isAuthorized() {
		JobAuthorizationContext jobAuthorizationContext = JobAuthorizationContext.get();
		if (jobAuthorizationContext != null) 
			return canBeUsedByBuildJobs && jobAuthorizationContext.isScriptAuthorized(this);

		Workspace workspace = Workspace.get();
		if (workspace != null)
			return canBeUsedByWorkspaceSpecs && (applicableWorkspaceProjects == null || WildcardUtils.matchPath(applicableWorkspaceProjects, workspace.getProject().getPath()));

		return true;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}
	
	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (jobAuthorization != null && io.onedev.server.job.match.JobMatch.parse(jobAuthorization, true, false).isUsingProject(projectPath))
			usage.add("authorization");
		if (Project.containsPath(applicableWorkspaceProjects, projectPath))
			usage.add("applicable workspace projects");
		return usage;
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		if (jobAuthorization != null) {
			var jobMatch = io.onedev.server.job.match.JobMatch.parse(jobAuthorization, true, false);
			jobMatch.onMoveProject(oldPath, newPath);
			jobAuthorization = jobMatch.toString();
		}
		applicableWorkspaceProjects = Project.substitutePath(applicableWorkspaceProjects, oldPath, newPath);
	}
	
}
