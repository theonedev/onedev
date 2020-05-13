package io.onedev.server.util.jobmatch;

import static io.onedev.server.model.Build.NAME_PROJECT;
import static io.onedev.server.util.jobmatch.JobMatch.getRuleName;
import static io.onedev.server.util.jobmatch.JobMatchLexer.Is;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private String projectName;
	
	public ProjectCriteria(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public boolean matches(Build build) {
		return WildcardUtils.matchString(projectName, build.getProject().getName());
	}

	@Override
	public void onRenameProject(String oldName, String newName) {
		if (oldName.equals(projectName))
			projectName = newName;
	}

	@Override
	public boolean isUsingProject(String projectName) {
		return projectName.equals(this.projectName);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_PROJECT) + " " + getRuleName(Is) + " " + quote(projectName);
	}
	
}
