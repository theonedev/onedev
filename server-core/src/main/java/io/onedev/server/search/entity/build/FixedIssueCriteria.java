package io.onedev.server.search.entity.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.service.BuildService;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.xodus.CommitInfoService;

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

	private CommitInfoService getCommitInfoManager() {
		return OneDev.getInstance(CommitInfoService.class);
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		BuildService buildService = OneDev.getInstance(BuildService.class);
		Path<Long> attribute = from.get(Build.PROP_NUMBER);
		List<Predicate> predicates = new ArrayList<>();
		issue.getProject().getTree().stream().filter(it->it.isCodeManagement()).forEach(it-> {
			Collection<ObjectId> fixCommits = getCommitInfoManager().getFixCommits(it.getId(), issue.getId(), true);
			Collection<String> descendants = new HashSet<>();
			for (ObjectId each: getCommitInfoManager().getDescendants(it.getId(), fixCommits))
				descendants.add(each.name());
			Collection<Long> inBuildNumbers = buildService.filterNumbers(it.getId(), descendants);
			predicates.add(builder.and(
					builder.equal(from.get(Build.PROP_PROJECT), it),
					forManyValues(builder, attribute, inBuildNumbers, buildService.getNumbers(it.getId()))));
		});
		if (!predicates.isEmpty())
			return builder.or(predicates.toArray(new Predicate[0]));
		else
			return builder.disjunction();
	}

	@Override
	public boolean matches(Build build) {
		Collection<ObjectId> fixCommits = getCommitInfoManager()
				.getFixCommits(build.getProject().getId(), issue.getId(), true); 
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
