package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.JobMatch;
import io.onedev.server.annotation.RegEx;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.util.usage.Usage;

@Editable
public class GroovyScript implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String BUILTIN_PREFIX = "builtin:";

	private String name;
	
	private List<String> content;
	
	private boolean canBeUsedByBuildJobs = true;
	
	private String authorization;

	@Editable(order=100)
	@RegEx(pattern ="^(?!" + BUILTIN_PREFIX + ").*$", message="Name is not allowed to start with '" + BUILTIN_PREFIX + "'")
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
	public String getAuthorization() {
		return authorization;
	}
	
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}
	
	public final boolean isAuthorized() {
		JobAuthorizationContext jobAuthorizationContext = JobAuthorizationContext.get();
		if (jobAuthorizationContext != null) 
			return canBeUsedByBuildJobs && jobAuthorizationContext.isScriptAuthorized(this);
		else 
			return true;
	}
	
	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (authorization != null && io.onedev.server.job.match.JobMatch.parse(authorization, true, false).isUsingProject(projectPath))
			usage.add("authorization");
		return usage;
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		if (authorization != null) {
			var jobMatch = io.onedev.server.job.match.JobMatch.parse(authorization, true, false);
			jobMatch.onMoveProject(oldPath, newPath);
			authorization = jobMatch.toString();
		}
	}
	
}
