package io.onedev.server.search.entity.issue;

import static io.onedev.server.search.entity.EntityQuery.quote;
import static io.onedev.server.search.entity.issue.IssueQuery.getRuleName;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.ProjectAwareCommitId;

public class FixedBetweenCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final int sinceType;
	
	private final String sinceValue;
	
	private final ObjectId sinceCommitId;
	
	private final int untilType;
	
	private final String untilValue;
	
	private final ObjectId untilCommitId;
	
	public FixedBetweenCriteria(@Nullable Project project, int sinceType, String sinceValue, int untilType, String untilValue) {
		this.sinceType = sinceType;
		this.sinceValue = sinceValue;
		this.untilType = untilType;
		this.untilValue = untilValue;

		ProjectAwareCommitId since = getCommitId(project, sinceType, sinceValue);
		ProjectAwareCommitId until = getCommitId(project, untilType, untilValue);
		sinceCommitId = since.getCommitId();
		untilCommitId = until.getCommitId();
		if (since.getProject().equals(until.getProject())) { 
			this.project = since.getProject();
		} else {
			throw new OneException("'" + getRuleName(IssueQueryLexer.FixedBetween) 
				+ "' should be used for same projects");
		}
	}
	
	private static ProjectAwareCommitId getCommitId(@Nullable Project project, int type, String value) {
		if (type == IssueQueryLexer.Build) {
			Build build = EntityQuery.getBuild(project, value);
			return new ProjectAwareCommitId(build.getProject(), build.getCommitId());
		} else {
			return EntityQuery.getCommitId(project, value);
		}
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Set<Long> fixedIssueNumbers = new HashSet<>();
		
		Repository repository = project.getRepository();
		try (RevWalk revWalk = new RevWalk(repository)) {
			revWalk.markStart(revWalk.parseCommit(untilCommitId));
			revWalk.markUninteresting(revWalk.parseCommit(sinceCommitId));

			RevCommit commit;
			while ((commit = revWalk.next()) != null) 
				fixedIssueNumbers.addAll(IssueUtils.parseFixedIssues(project, commit.getFullMessage()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Predicate issuePredicate;
		Path<Long> attribute = root.get(IssueConstants.ATTR_NUMBER);		
		if (fixedIssueNumbers.size() > IN_CLAUSE_LIMIT) {
			Collection<Long> allIssueNumbers = OneDev.getInstance(IssueManager.class).getIssueNumbers(project.getId());
			issuePredicate = inManyValues(builder, attribute, fixedIssueNumbers, allIssueNumbers);
		} else if (!fixedIssueNumbers.isEmpty()) {
			issuePredicate = root.get(IssueConstants.ATTR_NUMBER).in(fixedIssueNumbers);
		} else {
			issuePredicate = builder.disjunction();
		}
		return builder.and(builder.equal(root.get(IssueConstants.ATTR_PROJECT), project), issuePredicate);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		if (project.equals(issue.getProject())) {
			Repository repository = issue.getProject().getRepository();
			try (RevWalk revWalk = new RevWalk(repository)) {
				revWalk.markStart(revWalk.parseCommit(untilCommitId));
				revWalk.markUninteresting(revWalk.parseCommit(sinceCommitId));

				RevCommit commit;
				while ((commit = revWalk.next()) != null) { 
					if (IssueUtils.parseFixedIssues(project, commit.getFullMessage()).contains(issue.getNumber()))
						return true;
				}
				return false;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return false;
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
