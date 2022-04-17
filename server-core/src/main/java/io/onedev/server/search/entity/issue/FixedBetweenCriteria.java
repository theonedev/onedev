package io.onedev.server.search.entity.issue;

import static io.onedev.server.search.entity.issue.IssueQuery.getRuleName;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.Criteria;

public class FixedBetweenCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final int firstType;
	
	private final String firstValue;
	
	private final int secondType;
	
	private final String secondValue;
	
	private transient ProjectAndCommitIds projectAndCommitIds;
	
	public FixedBetweenCriteria(@Nullable Project project, int firstType, String firstValue, 
			int secondType, String secondValue) {
		this.project = project;
		this.firstType = firstType;
		this.firstValue = firstValue;
		this.secondType = secondType;
		this.secondValue = secondValue;
	}
	
	private ProjectAndCommitIds getProjectAndCommitIds() {
		if (projectAndCommitIds == null) {
			ProjectScopedCommit first = getCommitId(project, firstType, firstValue);
			ProjectScopedCommit second = getCommitId(project, secondType, secondValue);
			if (first.getProject().equals(second.getProject())) { 
				projectAndCommitIds = new ProjectAndCommitIds(
						first.getProject(), first.getCommitId(), second.getCommitId());
			} else {
				throw new ExplicitException("'" + getRuleName(IssueQueryLexer.FixedBetween) 
					+ "' should be used for same projects");
			}
		}
		return projectAndCommitIds;
	}
	
	private static ProjectScopedCommit getCommitId(@Nullable Project project, int type, String value) {
		if (type == IssueQueryLexer.Build) {
			Build build = EntityQuery.getBuild(project, value);
			return new ProjectScopedCommit(build.getProject(), build.getCommitId());
		} else {
			return EntityQuery.getCommitId(project, value);
		}
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Set<Long> fixedIssueNumbers = new HashSet<>();
		
		Project project = getProjectAndCommitIds().project;
		ObjectId firstCommitId = getProjectAndCommitIds().firstCommitId;
		ObjectId secondCommitId = getProjectAndCommitIds().secondCommitId;
		Repository repository = getProjectAndCommitIds().project.getRepository();
		ObjectId mergeBaseId = GitUtils.getMergeBase(repository, firstCommitId, secondCommitId);
		if (mergeBaseId != null) {
			try (RevWalk revWalk = new RevWalk(repository)) {
				revWalk.markStart(revWalk.parseCommit(secondCommitId));
				revWalk.markStart(revWalk.parseCommit(firstCommitId));
				revWalk.markUninteresting(revWalk.parseCommit(mergeBaseId));

				RevCommit commit;
				while ((commit = revWalk.next()) != null) 
					fixedIssueNumbers.addAll(Issue.parseFixedIssueNumbers(project, commit.getFullMessage()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		Predicate issuePredicate;
		Path<Long> attribute = from.get(Issue.PROP_NUMBER);		
		if (fixedIssueNumbers.size() > IN_CLAUSE_LIMIT) {
			Collection<Long> allIssueNumbers = OneDev.getInstance(IssueManager.class).getIssueNumbers(project.getId());
			issuePredicate = forManyValues(builder, attribute, fixedIssueNumbers, allIssueNumbers);
		} else if (!fixedIssueNumbers.isEmpty()) {
			issuePredicate = from.get(Issue.PROP_NUMBER).in(fixedIssueNumbers);
		} else {
			issuePredicate = builder.disjunction();
		}
		return builder.and(
				builder.equal(from.get(Issue.PROP_PROJECT), project), 
				issuePredicate);
	}

	@Override
	public boolean matches(Issue issue) {
		Project project = getProjectAndCommitIds().project;
		ObjectId firstCommitId = getProjectAndCommitIds().firstCommitId;
		ObjectId secondCommitId = getProjectAndCommitIds().secondCommitId;
		if (project.equals(issue.getProject())) {
			Repository repository = issue.getProject().getRepository();
			ObjectId mergeBaseId = GitUtils.getMergeBase(repository, firstCommitId, secondCommitId);
			if (mergeBaseId != null) {
				try (RevWalk revWalk = new RevWalk(repository)) {
					revWalk.markStart(revWalk.parseCommit(secondCommitId));
					revWalk.markStart(revWalk.parseCommit(firstCommitId));
					revWalk.markUninteresting(revWalk.parseCommit(mergeBaseId));

					RevCommit commit;
					while ((commit = revWalk.next()) != null) { 
						if (Issue.parseFixedIssueNumbers(issue.getProject(), commit.getFullMessage()).contains(issue.getNumber()))
							return true;
					}
					return false;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}			
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return getRuleName(IssueQueryLexer.FixedBetween) + " " 
				+ getRuleName(firstType) + " " + quote(firstValue) + " " 
				+ getRuleName(IssueQueryLexer.And) + " " 
				+ getRuleName(secondType) + " " + quote(secondValue);
	}

	private static class ProjectAndCommitIds {
		
		final Project project;
		
		final ObjectId firstCommitId;
		
		final ObjectId secondCommitId;
		
		ProjectAndCommitIds(Project project, ObjectId firstCommitId, ObjectId secondCommitId) {
			this.project = project;
			this.firstCommitId = firstCommitId;
			this.secondCommitId = secondCommitId;
		}
		
	}
}
