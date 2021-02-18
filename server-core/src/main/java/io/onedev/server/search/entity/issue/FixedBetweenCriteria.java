package io.onedev.server.search.entity.issue;

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

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.ProjectScopedCommit;

public class FixedBetweenCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final int firstType;
	
	private final String firstValue;
	
	private final ObjectId firstCommitId;
	
	private final int secondType;
	
	private final String secondValue;
	
	private final ObjectId secondCommitId;
	
	public FixedBetweenCriteria(@Nullable Project project, int firstType, String firstValue, 
			int secondType, String secondValue) {
		this.firstType = firstType;
		this.firstValue = firstValue;
		this.secondType = secondType;
		this.secondValue = secondValue;

		ProjectScopedCommit first = getCommitId(project, firstType, firstValue);
		ProjectScopedCommit second = getCommitId(project, secondType, secondValue);
		firstCommitId = first.getCommitId();
		secondCommitId = second.getCommitId();
		if (first.getProject().equals(second.getProject())) { 
			this.project = first.getProject();
		} else {
			throw new ExplicitException("'" + getRuleName(IssueQueryLexer.FixedBetween) 
				+ "' should be used for same projects");
		}
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
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Set<Long> fixedIssueNumbers = new HashSet<>();
		
		Repository repository = project.getRepository();
		ObjectId mergeBaseId = GitUtils.getMergeBase(repository, firstCommitId, secondCommitId);
		if (mergeBaseId != null) {
			try (RevWalk revWalk = new RevWalk(repository)) {
				revWalk.markStart(revWalk.parseCommit(secondCommitId));
				revWalk.markStart(revWalk.parseCommit(firstCommitId));
				revWalk.markUninteresting(revWalk.parseCommit(mergeBaseId));

				RevCommit commit;
				while ((commit = revWalk.next()) != null) 
					fixedIssueNumbers.addAll(IssueUtils.parseFixedIssueNumbers(project, commit.getFullMessage()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		Predicate issuePredicate;
		Path<Long> attribute = root.get(Issue.PROP_NUMBER);		
		if (fixedIssueNumbers.size() > IN_CLAUSE_LIMIT) {
			Collection<Long> allIssueNumbers = OneDev.getInstance(IssueManager.class).getIssueNumbers(project.getId());
			issuePredicate = inManyValues(builder, attribute, fixedIssueNumbers, allIssueNumbers);
		} else if (!fixedIssueNumbers.isEmpty()) {
			issuePredicate = root.get(Issue.PROP_NUMBER).in(fixedIssueNumbers);
		} else {
			issuePredicate = builder.disjunction();
		}
		return builder.and(
				builder.equal(root.get(Issue.PROP_PROJECT), project), 
				issuePredicate);
	}

	@Override
	public boolean matches(Issue issue) {
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
						if (IssueUtils.parseFixedIssueNumbers(issue.getProject(), commit.getFullMessage()).contains(issue.getNumber()))
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

}
