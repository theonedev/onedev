package io.onedev.server.job.match;

import static io.onedev.server.job.match.JobMatch.getRuleName;
import static io.onedev.server.model.Build.NAME_PROJECT;

import io.onedev.commons.utils.PathUtils;

import static io.onedev.server.job.match.JobMatchLexer.Is;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private String projectPath;
	
	public ProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public boolean matches(Build build) {
		return WildcardUtils.matchPath(projectPath, build.getProject().getPath());
	}

	@Override
	public void onRenameProject(String oldPath, String newPath) {
		projectPath = PathUtils.substituteSelfOrAncestor(projectPath, oldPath, newPath);
	}

	@Override
	public boolean isUsingProject(String projectPath) {
		return PathUtils.isSelfOrAncestor(projectPath, this.projectPath);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_PROJECT) + " " + getRuleName(Is) + " " + quote(projectPath);
	}
	
}
