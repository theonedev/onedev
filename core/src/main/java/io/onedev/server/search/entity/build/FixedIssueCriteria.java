package io.onedev.server.search.entity.build;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.BuildConstants;

public class FixedIssueCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private Issue value;
	
	public FixedIssueCriteria(Issue value) {
		this.value = value;
	}

	private CommitInfoManager getCommitInfoManager() {
		return OneDev.getInstance(CommitInfoManager.class);
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<Long> attribute = context.getRoot().get(BuildConstants.ATTR_ID);
		Collection<ObjectId> fixCommits = getCommitInfoManager().getFixCommits(project, value.getNumber());
		Collection<String> descendents = new HashSet<>();
		for (ObjectId each: getCommitInfoManager().getDescendants(project, fixCommits))
			descendents.add(each.name());
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
		Collection<Long> inBuildIds = cacheManager.filterBuildIds(project.getId(), descendents);
		return inManyValues(context.getBuilder(), attribute, inBuildIds, cacheManager.getBuildIdsByProject(project.getId()));
	}

	@Override
	public boolean matches(Build build, User user) {
		Collection<ObjectId> fixCommits = getCommitInfoManager().getFixCommits(build.getConfiguration().getProject(), value.getNumber()); 
		for (ObjectId commit: fixCommits) {
			ObjectId buildCommit = ObjectId.fromString(build.getCommitHash());
			if (GitUtils.isMergedInto(build.getConfiguration().getProject().getRepository(), null, commit, buildCommit))
				return true;
		}
		return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.FixedIssue) + " " + BuildQuery.quote("#" + value.getNumber());
	}

}
