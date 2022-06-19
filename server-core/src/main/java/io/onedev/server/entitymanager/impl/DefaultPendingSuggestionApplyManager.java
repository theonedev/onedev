package io.onedev.server.entitymanager.impl;

import java.util.HashMap;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PendingSuggestionApplyManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PendingSuggestionApply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPendingSuggestionApplyManager extends BaseEntityManager<PendingSuggestionApply> 
		implements PendingSuggestionApplyManager {

	private final SettingManager settingManager;
	
	@Inject
	public DefaultPendingSuggestionApplyManager(Dao dao, SettingManager settingManager) {
		super(dao);
		this.settingManager = settingManager;
	}

	@Transactional
	@Override
	public void apply(User user, PullRequest request, boolean resolveComment, String commitMessage) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PendingSuggestionApply> criteriaQuery = 
				builder.createQuery(PendingSuggestionApply.class);
		Root<PendingSuggestionApply> root = criteriaQuery.from(PendingSuggestionApply.class);

		criteriaQuery.where(builder.and(
				builder.equal(root.get(PendingSuggestionApply.PROP_REQUEST), request), 
				builder.equal(root.get(PendingSuggestionApply.PROP_USER), user)));
		
		ObjectId headCommitId = request.getSourceHead();
		
		BlobEdits blobEdits = new BlobEdits(new HashSet<>(), new HashMap<>());
		for (PendingSuggestionApply pendingApply: getSession().createQuery(criteriaQuery).list()) {
			pendingApply.getComment().applySuggestion(request.getSourceProject(), headCommitId, 
					blobEdits, pendingApply.getSuggestion());
			delete(pendingApply);
		}
		
		String sourceBranch = request.getSourceBranch();
		
		ObjectId newCommitId = blobEdits.commit(
				request.getSourceProject().getRepository(), sourceBranch, 
				headCommitId, headCommitId, user.asPerson(), commitMessage, 
				settingManager.getGpgSetting().getSigningKey());
		
		Long projectId = request.getSourceProject().getId();
		String refName = GitUtils.branch2ref(sourceBranch);
		OneDev.getInstance(TransactionManager.class).runAfterCommit(new Runnable() {

			@Override
			public void run() {
				OneDev.getInstance(SessionManager.class).runAsync(new Runnable() {

					@Override
					public void run() {
						Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
						project.cacheObjectId(sourceBranch, newCommitId);
						RefUpdated refUpdated = new RefUpdated(project, refName, headCommitId, newCommitId);
						OneDev.getInstance(ListenerRegistry.class).post(refUpdated);
					}
					
				});
			}
			
		});
		
	}

	@Transactional
	@Override
	public void discard(User user, PullRequest request) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaDelete<PendingSuggestionApply> criteriaDelete = builder.createCriteriaDelete(PendingSuggestionApply.class);
		Root<PendingSuggestionApply> root = criteriaDelete.from(PendingSuggestionApply.class);

		if (request != null) {
			criteriaDelete.where(builder.and(
					builder.equal(root.get(PendingSuggestionApply.PROP_REQUEST), request), 
					builder.equal(root.get(PendingSuggestionApply.PROP_USER), user)));
		} else {
			criteriaDelete.where(builder.equal(root.get(PendingSuggestionApply.PROP_USER), user));
		}

		getSession().createQuery(criteriaDelete).executeUpdate();
	}

	@Sessional
	@Override
	public int count(User user, PullRequest request) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<PendingSuggestionApply> root = criteriaQuery.from(PendingSuggestionApply.class);

		criteriaQuery.where(builder.and(
				builder.equal(root.get(PendingSuggestionApply.PROP_REQUEST), request), 
				builder.equal(root.get(PendingSuggestionApply.PROP_USER), user)));

		criteriaQuery.select(builder.count(root));
		
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

}
