package io.onedev.server.search.entity.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.service.GitService;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class FixedIssueCriteria extends Criteria<Build> {

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
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		Path<Long> attribute = from.get(Build.PROP_NUMBER);
		List<Predicate> predicates = new ArrayList<>();
		issue.getProject().getTree().stream().filter(it->it.isCodeManagement()).forEach(it-> {
			Collection<ObjectId> fixCommits = getCommitInfoManager().getFixCommits(it.getId(), issue.getId());
			Collection<String> descendants = new HashSet<>();
			for (ObjectId each: getCommitInfoManager().getDescendants(it.getId(), fixCommits))
				descendants.add(each.name());
			Collection<Long> inBuildNumbers = buildManager.filterNumbers(it.getId(), descendants);
			predicates.add(builder.and(
					builder.equal(from.get(Build.PROP_PROJECT), it),
					forManyValues(builder, attribute, inBuildNumbers, buildManager.getNumbersByProject(it.getId()))));
		});
		if (!predicates.isEmpty())
			return builder.or(predicates.toArray(new Predicate[0]));
		else
			return builder.disjunction();
	}

	@Override
	public boolean matches(Build build) {
		Collection<ObjectId> fixCommits = getCommitInfoManager()
				.getFixCommits(build.getProject().getId(), issue.getId()); 
		GitService gitService = OneDev.getInstance(GitService.class);
		for (ObjectId commit: fixCommits) {
			ObjectId buildCommit = ObjectId.fromString(build.getCommitHash());
			if (gitService.isMergedInto(build.getProject(), null, commit, buildCommit))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.FixedIssue) + " " + quote(value);
	}

}
