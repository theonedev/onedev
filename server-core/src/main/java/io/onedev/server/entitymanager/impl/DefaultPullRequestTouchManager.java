package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestTouchManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.*;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static io.onedev.server.model.PullRequestTouch.PROP_REQUEST_ID;
import static java.lang.String.format;

@Singleton
public class DefaultPullRequestTouchManager extends BaseEntityManager<PullRequestTouch> 
		implements PullRequestTouchManager {

	private final ProjectManager projectManager;

	private final TransactionManager transactionManager;

	@Inject
	public DefaultPullRequestTouchManager(Dao dao, ProjectManager projectManager, TransactionManager transactionManager) {
		super(dao);
		this.projectManager = projectManager;
		this.transactionManager = transactionManager;
	}

	@Transactional
	@Override
	public void touch(Project project, Long requestId) {
		var projectId = project.getId();
		transactionManager.runAfterCommit(() -> transactionManager.runAsync(() -> {
			var innerProject = projectManager.load(projectId);
			var query = getSession().createQuery(format("delete from PullRequestTouch where project=:project and %s=:%s", PROP_REQUEST_ID, PROP_REQUEST_ID));
			query.setParameter("project", innerProject);
			query.setParameter(PROP_REQUEST_ID, requestId);
			query.executeUpdate();

			var touch = new PullRequestTouch();
			touch.setProject(innerProject);
			touch.setRequestId(requestId);
			dao.persist(touch);
		}));
		
	}

	@Sessional
	@Override
	public List<PullRequestTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count) {
		EntityCriteria<PullRequestTouch> criteria = EntityCriteria.of(PullRequestTouch.class);
		criteria.add(Restrictions.eq("project.id", projectId));
		criteria.add(Restrictions.gt(AbstractEntity.PROP_ID, afterTouchId));
		return dao.query(criteria, 0, count);
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof PullRequest) {
			PullRequest request = (PullRequest) event.getEntity();
			touch(request.getProject(), request.getId());
		} else if (event.getEntity() instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) event.getEntity();
			touch(comment.getRequest().getProject(), comment.getRequest().getId());
		} else if (event.getEntity() instanceof PullRequestUpdate) {
			PullRequestUpdate update = (PullRequestUpdate) event.getEntity();
			touch(update.getRequest().getProject(), update.getRequest().getId());
		} 
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof PullRequest) {
			PullRequest request = (PullRequest) event.getEntity();
			touch(request.getProject(), request.getId());
		} else if (event.getEntity() instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) event.getEntity();
			touch(comment.getRequest().getProject(), comment.getRequest().getId());
		} else if (event.getEntity() instanceof PullRequestUpdate) {
			PullRequestUpdate update = (PullRequestUpdate) event.getEntity();
			touch(update.getRequest().getProject(), update.getRequest().getId());
		}
	}
	
}