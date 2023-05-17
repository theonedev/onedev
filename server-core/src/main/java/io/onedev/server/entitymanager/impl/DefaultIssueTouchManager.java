package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.IssueTouchManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.*;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;

import static io.onedev.server.model.IssueTouch.PROP_ISSUE_ID;
import static java.lang.String.format;

@Singleton
public class DefaultIssueTouchManager extends BaseEntityManager<IssueTouch> 
		implements IssueTouchManager {

	@Inject
    public DefaultIssueTouchManager(Dao dao) {
        super(dao);
    }

	@Transactional
	@Override
	public void touch(Project project, Long issueId) {
		var query = getSession().createQuery(format("delete from IssueTouch where project=:project and %s=:%s", PROP_ISSUE_ID, PROP_ISSUE_ID));
		query.setParameter("project", project);
		query.setParameter(PROP_ISSUE_ID, issueId);
		query.executeUpdate();
		
		var touch = new IssueTouch();
		touch.setProject(project);
		touch.setIssueId(issueId);
		dao.persist(touch);
	}

	@Sessional
	@Override
	public List<IssueTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count) {
		EntityCriteria<IssueTouch> criteria = EntityCriteria.of(IssueTouch.class);
		criteria.add(Restrictions.eq("project.id", projectId));
		criteria.add(Restrictions.gt(AbstractEntity.PROP_ID, afterTouchId));
		return dao.query(criteria, 0, count);
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			touch(issue.getProject(), issue.getId());
		} else if (event.getEntity() instanceof IssueComment) {
			IssueComment comment = (IssueComment) event.getEntity();			
			touch(comment.getIssue().getProject(), comment.getIssue().getId());
		} else if (event.getEntity() instanceof IssueField) {
			IssueField field = (IssueField) event.getEntity();
			touch(field.getIssue().getProject(), field.getIssue().getId());
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			touch(issue.getProject(), issue.getId());
		} else if (event.getEntity() instanceof IssueComment) {
			IssueComment comment = (IssueComment) event.getEntity();
			touch(comment.getIssue().getProject(), comment.getIssue().getId());
		} else if (event.getEntity() instanceof IssueField) {
			IssueField field = (IssueField) event.getEntity();
			touch(field.getIssue().getProject(), field.getIssue().getId());
		}
	}
	
}