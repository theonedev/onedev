package io.onedev.server.search.entity.issue;

import static io.onedev.server.search.entity.EntityQuery.quote;
import static io.onedev.server.search.entity.issue.IssueQuery.getRuleName;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.IssueUtils;

public class FixedBetweenCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int sinceType;
	
	private final String sinceValue;
	
	private final ObjectId sinceCommitId;
	
	private final int untilType;
	
	private final String untilValue;
	
	private final ObjectId untilCommitId;
	
	public FixedBetweenCriteria(int sinceType, String sinceValue, ObjectId sinceCommitId, 
			int untilType, String untilValue, ObjectId untilCommitId) {
		this.sinceType = sinceType;
		this.sinceValue = sinceValue;
		this.sinceCommitId = sinceCommitId;
		this.untilType = untilType;
		this.untilValue = untilValue;
		this.untilCommitId = untilCommitId;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		Set<Long> fixedIssueNumbers = new HashSet<>();
		
		Repository repository = project.getRepository();
		try (RevWalk revWalk = new RevWalk(repository)) {
			revWalk.markStart(revWalk.parseCommit(untilCommitId));
			revWalk.markUninteresting(revWalk.parseCommit(sinceCommitId));

			RevCommit commit;
			while ((commit = revWalk.next()) != null) 
				fixedIssueNumbers.addAll(IssueUtils.parseFixedIssues(commit.getFullMessage()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Path<Long> attribute = context.getRoot().get(IssueConstants.ATTR_NUMBER);		
		if (fixedIssueNumbers.size() > IN_CLAUSE_LIMIT) {
			Collection<Long> allIssueNumbers = OneDev.getInstance(CacheManager.class).getIssueNumbers(project.getId());
			return inManyValues(context.getBuilder(), attribute, fixedIssueNumbers, allIssueNumbers);
		} else if (!fixedIssueNumbers.isEmpty()) {
			return context.getRoot().get(IssueConstants.ATTR_NUMBER).in(fixedIssueNumbers);
		} else {
			return context.getBuilder().disjunction();
		}
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Repository repository = issue.getProject().getRepository();
		try (RevWalk revWalk = new RevWalk(repository)) {
			revWalk.markStart(revWalk.parseCommit(untilCommitId));
			revWalk.markUninteresting(revWalk.parseCommit(sinceCommitId));

			RevCommit commit;
			while ((commit = revWalk.next()) != null) { 
				if (IssueUtils.parseFixedIssues(commit.getFullMessage()).contains(issue.getNumber()))
					return true;
			}
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return getRuleName(IssueQueryLexer.FixedBetween) + " " 
				+ getRuleName(sinceType) + " " + quote(sinceValue) + " " 
				+ getRuleName(IssueQueryLexer.And) + " " 
				+ getRuleName(untilType) + " " + quote(untilValue);
	}

}
