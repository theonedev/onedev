package io.onedev.server.search.entity.build;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class FixedIssueCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final Issue issue;
	
	private final String value;
	
	public FixedIssueCriteria(@Nullable Project project, String value) {
		issue = EntityQuery.getIssue(project, value);
		this.value = value;
	}
	
	public FixedIssueCriteria(Issue issue) {
		this.issue = issue;
		value = String.valueOf(issue.getNumber());
	}

	private CommitInfoManager getCommitInfoManager() {
		return OneDev.getInstance(CommitInfoManager.class);
	}
	
	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<Long> attribute = root.get(Build.PROP_ID);
		Project project = issue.getProject();
		Collection<ObjectId> fixCommits = getCommitInfoManager().getFixCommits(project, issue.getNumber());
		Collection<String> descendents = new HashSet<>();
		for (ObjectId each: getCommitInfoManager().getDescendants(project, fixCommits))
			descendents.add(each.name());
		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		Collection<Long> inBuildIds = buildManager.filterIds(project.getId(), descendents);
		return builder.and(
				builder.equal(root.get(Build.PROP_PROJECT), issue.getProject()),
				inManyValues(builder, attribute, inBuildIds, buildManager.getIdsByProject(project.getId())));
	}

	@Override
	public boolean matches(Build build) {
		if (build.getProject().equals(issue.getProject())) {
			Collection<ObjectId> fixCommits = getCommitInfoManager().getFixCommits(build.getProject(), issue.getNumber()); 
			for (ObjectId commit: fixCommits) {
				ObjectId buildCommit = ObjectId.fromString(build.getCommitHash());
				if (GitUtils.isMergedInto(build.getProject().getRepository(), null, commit, buildCommit))
					return true;
			}
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.FixedIssue) + " " + quote(value);
	}

}
