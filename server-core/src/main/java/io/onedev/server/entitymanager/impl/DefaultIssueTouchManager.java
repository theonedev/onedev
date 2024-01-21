package io.onedev.server.entitymanager.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.server.entitymanager.IssueTouchManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.issue.*;
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static io.onedev.server.model.IssueTouch.PROP_ISSUE_ID;
import static io.onedev.server.model.IssueTouch.PROP_PROJECT;

@Singleton
public class DefaultIssueTouchManager extends BaseEntityManager<IssueTouch> 
		implements IssueTouchManager {
	
	private static final int BATCH_SIZE = 500;
	
	private final ProjectManager projectManager;
	
	private final TransactionManager transactionManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
    public DefaultIssueTouchManager(Dao dao, ProjectManager projectManager, TransactionManager transactionManager, 
									ListenerRegistry listenerRegistry) {
        super(dao);
		this.projectManager = projectManager;
		this.transactionManager = transactionManager;
		this.listenerRegistry = listenerRegistry;
    }

	@Transactional
	@Override
	public void touch(Project project, Collection<Long> issueIds, boolean newIssues) {
		var projectId = project.getId();
		transactionManager.runAfterCommit(() -> transactionManager.runAsync(() -> {
			var innerProject = projectManager.load(projectId);
			
			if (!newIssues) {
				for (var partition: Lists.partition(new ArrayList<>(issueIds), BATCH_SIZE)) {
					CriteriaBuilder builder = getSession().getCriteriaBuilder();
					CriteriaDelete<IssueTouch> criteriaDelete = builder.createCriteriaDelete(IssueTouch.class);
					Root<IssueTouch> root = criteriaDelete.from(IssueTouch.class);
					criteriaDelete.where(
							builder.equal(root.get(PROP_PROJECT), innerProject),
							root.get(PROP_ISSUE_ID).in(partition));
					getSession().createQuery(criteriaDelete).executeUpdate();
				}
			}
			
			for (var issueId: issueIds) {
				var touch = new IssueTouch();
				touch.setProject(innerProject);
				touch.setIssueId(issueId);
				dao.persist(touch);
			}
			listenerRegistry.post(new IssuesTouched(innerProject, issueIds));
		}));
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
	public void on(IssueOpened event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), true);
	}

	@Transactional
	@Listen
	public void on(IssueCommentCreated event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), false);
	}

	@Transactional
	@Listen
	public void on(IssueCommentEdited event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), false);
	}

	@Transactional
	@Listen
	public void on(IssueChanged event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), false);
	}

	@Transactional
	@Listen
	public void on(IssuesImported event) {
		touch(event.getProject(), event.getIssueIds(), true);
	}

	@Transactional
	@Listen
	public void on(IssuesMoved event) {
		touch(event.getProject(), event.getIssueIds(), true);
	}

	@Transactional
	@Listen
	public void on(IssuesCopied event) {
		touch(event.getProject(), new HashSet<>(event.getIssueIdMapping().values()), true);
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			touch(issue.getProject(), Sets.newHashSet(issue.getId()), false);
		} else if (event.getEntity() instanceof IssueComment) {
			IssueComment comment = (IssueComment) event.getEntity();
			touch(comment.getIssue().getProject(), Sets.newHashSet(comment.getIssue().getId()), false);
		} else if (event.getEntity() instanceof IssueField) {
			IssueField field = (IssueField) event.getEntity();
			touch(field.getIssue().getProject(), Sets.newHashSet(field.getIssue().getId()), false);
		}
	}
	
}