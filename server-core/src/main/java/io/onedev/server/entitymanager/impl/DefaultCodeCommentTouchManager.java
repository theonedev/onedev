package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.CodeCommentTouchManager;
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

import static io.onedev.server.model.CodeCommentTouch.PROP_COMMENT_ID;
import static java.lang.String.format;

@Singleton
public class DefaultCodeCommentTouchManager extends BaseEntityManager<CodeCommentTouch> 
		implements CodeCommentTouchManager {

	@Inject
    public DefaultCodeCommentTouchManager(Dao dao) {
        super(dao);
    }

	@Transactional
	@Override
	public void touch(Project project, Long commentId) {
		var query = getSession().createQuery(format("delete from CodeCommentTouch where project=:project and %s=:%s", PROP_COMMENT_ID, PROP_COMMENT_ID));
		query.setParameter("project", project);
		query.setParameter(PROP_COMMENT_ID, commentId);
		query.executeUpdate();
		
		var touch = new CodeCommentTouch();
		touch.setProject(project);
		touch.setCommentId(commentId);
		dao.persist(touch);
	}

	@Sessional
	@Override
	public List<CodeCommentTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count) {
		EntityCriteria<CodeCommentTouch> criteria = EntityCriteria.of(CodeCommentTouch.class);
		criteria.add(Restrictions.eq("project.id", projectId));
		criteria.add(Restrictions.gt(AbstractEntity.PROP_ID, afterTouchId));
		return dao.query(criteria, 0, count);
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof CodeComment) {
			CodeComment comment = (CodeComment) event.getEntity();
			touch(comment.getProject(), comment.getId());
		} else if (event.getEntity() instanceof CodeCommentReply) {
			CodeCommentReply reply = (CodeCommentReply) event.getEntity();
			touch(reply.getComment().getProject(), reply.getComment().getId());
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof CodeComment) {
			CodeComment comment = (CodeComment) event.getEntity();
			touch(comment.getProject(), comment.getId());
		} else if (event.getEntity() instanceof CodeCommentReply) {
			CodeCommentReply reply = (CodeCommentReply) event.getEntity();
			touch(reply.getComment().getProject(), reply.getComment().getId());
		}
	}
	
}