package io.onedev.server.job.requirement;

import static io.onedev.server.job.requirement.JobRequirement.getRuleName;
import static io.onedev.server.job.requirement.JobRequirementLexer.Is;
import static io.onedev.server.model.Build.NAME_PROJECT;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCriteria extends Criteria<ProjectAndBranch> {

	private static final long serialVersionUID = 1L;
	
	private String projectPath;
	
	public ProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public boolean matches(ProjectAndBranch projectAndBranch) {
		return WildcardUtils.matchPath(projectPath, projectAndBranch.getProject().getPath());
	}

	@Override
	public void onMoveProject(String oldPath, String newPath) {
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
