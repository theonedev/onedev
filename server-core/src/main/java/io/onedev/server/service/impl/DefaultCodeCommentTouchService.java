package io.onedev.server.service.impl;

import static io.onedev.server.model.CodeCommentTouch.PROP_COMMENT_ID;
import static java.lang.String.format;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.service.CodeCommentTouchService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.codecomment.CodeCommentCreated;
import io.onedev.server.event.project.codecomment.CodeCommentEdited;
import io.onedev.server.event.project.codecomment.CodeCommentReplyCreated;
import io.onedev.server.event.project.codecomment.CodeCommentReplyDeleted;
import io.onedev.server.event.project.codecomment.CodeCommentReplyEdited;
import io.onedev.server.event.project.codecomment.CodeCommentTouched;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentTouch;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultCodeCommentTouchService extends BaseEntityService<CodeCommentTouch>
		implements CodeCommentTouchService {

	@Inject
	private ProjectService projectService;

	@Inject
	private TransactionService transactionService;

	@Inject
    private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void touch(Project project, Long commentId, boolean newComment) {
		var projectId = project.getId();
		transactionService.runAfterCommit(() -> transactionService.runAsync(() -> {
            var innerProject = projectService.load(projectId);
            if (!newComment) {
                var query = getSession().createQuery(format("delete from CodeCommentTouch where project=:project and %s=:%s", PROP_COMMENT_ID, PROP_COMMENT_ID));
                query.setParameter("project", innerProject);
                query.setParameter(PROP_COMMENT_ID, commentId);
                query.executeUpdate();
            }

			var touch = new CodeCommentTouch();
			touch.setProject(innerProject);
			touch.setCommentId(commentId);
			dao.persist(touch);

			listenerRegistry.post(new CodeCommentTouched(innerProject, commentId));
		}));
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
	public void on(CodeCommentCreated event) {
		touch(event.getProject(), event.getComment().getId(), true);
	}

	@Transactional
	@Listen
	public void on(CodeCommentEdited event) {
		touch(event.getProject(), event.getComment().getId(), false);
	}

	@Transactional
	@Listen
	public void on(CodeCommentReplyCreated event) {
		touch(event.getProject(), event.getComment().getId(), false);
	}

	@Transactional
	@Listen
	public void on(CodeCommentReplyEdited event) {
		touch(event.getProject(), event.getComment().getId(), false);
	}

	@Transactional
	@Listen
	public void on(CodeCommentReplyDeleted event) {
		touch(event.getProject(), event.getComment().getId(), false);
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof CodeComment) {
			CodeComment comment = (CodeComment) event.getEntity();
			touch(comment.getProject(), comment.getId(), false);
		}
	}
	
}