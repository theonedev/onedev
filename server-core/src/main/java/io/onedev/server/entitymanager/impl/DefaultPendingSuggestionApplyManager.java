package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentStatusChangeManager;
import io.onedev.server.entitymanager.PendingSuggestionApplyManager;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.PendingSuggestionApply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPendingSuggestionApplyManager extends BaseEntityManager<PendingSuggestionApply> 
		implements PendingSuggestionApplyManager {

	private final GitService gitService;
	
	@Inject
	public DefaultPendingSuggestionApplyManager(Dao dao, GitService gitService) {
		super(dao);
		this.gitService = gitService;
	}

	@Transactional
	@Override
	public ObjectId apply(User user, PullRequest request, String commitMessage) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PendingSuggestionApply> criteriaQuery = 
				builder.createQuery(PendingSuggestionApply.class);
		Root<PendingSuggestionApply> root = criteriaQuery.from(PendingSuggestionApply.class);

		criteriaQuery.where(builder.and(
				builder.equal(root.get(PendingSuggestionApply.PROP_REQUEST), request), 
				builder.equal(root.get(PendingSuggestionApply.PROP_USER), user)));
		
		ObjectId headCommitId = request.getLatestUpdate().getHeadCommit();
		
		BlobEdits blobEdits = new BlobEdits();
		
		List<CodeComment> unresolvedComments = new ArrayList<>();
		for (PendingSuggestionApply pendingApply: getSession().createQuery(criteriaQuery).list()) {
			CodeComment comment = pendingApply.getComment();
			unresolvedComments.add(comment);
			blobEdits.applySuggestion(request.getSourceProject(), comment.getMark(), 
					pendingApply.getSuggestion(), headCommitId); 
			delete(pendingApply);
		}
		
		String refName = GitUtils.branch2ref(request.getSourceBranch());
		
		ObjectId newCommitId = gitService.commit(request.getSourceProject(), 
				blobEdits, refName, headCommitId, headCommitId, user.asPerson(), 
				commitMessage, false);

		for (CodeComment comment: unresolvedComments) {
			CodeCommentStatusChange change = new CodeCommentStatusChange();
			change.setComment(comment);
			change.setResolved(true);
			change.setUser(SecurityUtils.getUser());
			CompareContext compareContext = new CompareContext();
			compareContext.setPullRequest(request);
			compareContext.setOldCommitHash(comment.getMark().getCommitHash());
			compareContext.setNewCommitHash(newCommitId.name());
			change.setCompareContext(compareContext);
			OneDev.getInstance(CodeCommentStatusChangeManager.class).save(change, "Suggestion applied");
		}

		return newCommitId;
	}

	@Transactional
	@Override
	public void discard(User user, PullRequest request) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaDelete<PendingSuggestionApply> criteriaDelete = builder.createCriteriaDelete(PendingSuggestionApply.class);
		Root<PendingSuggestionApply> root = criteriaDelete.from(PendingSuggestionApply.class);

		if (user != null) {
			criteriaDelete.where(builder.and(
					builder.equal(root.get(PendingSuggestionApply.PROP_REQUEST), request), 
					builder.equal(root.get(PendingSuggestionApply.PROP_USER), user)));
		} else {
			criteriaDelete.where(builder.equal(root.get(PendingSuggestionApply.PROP_REQUEST), request));
		}

		getSession().createQuery(criteriaDelete).executeUpdate();
	}

	@Sessional
	@Override
	public List<PendingSuggestionApply> query(User user, PullRequest request) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PendingSuggestionApply> criteriaQuery = builder.createQuery(PendingSuggestionApply.class);
		Root<PendingSuggestionApply> root = criteriaQuery.from(PendingSuggestionApply.class);

		criteriaQuery.where(builder.and(
				builder.equal(root.get(PendingSuggestionApply.PROP_REQUEST), request), 
				builder.equal(root.get(PendingSuggestionApply.PROP_USER), user)));

		criteriaQuery.select(root);
		
		return getSession().createQuery(criteriaQuery).list();
	}

}
