package io.onedev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import org.hibernate.criterion.Restrictions;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.manager.TaskManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Task;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.persistence.dao.EntityRemoved;

@Singleton
public class DefaultTaskManager extends AbstractEntityManager<Task> implements TaskManager {
	
	@Inject
	public DefaultTaskManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			Query query = getSession().createQuery("delete from Task where project=:project and type=:type and source=:source");
			query.setParameter("project", issue.getProject());
			query.setParameter("type", Task.TYPE_ISSUE);
			query.setParameter("source", issue.getNumberStr());
			query.executeUpdate();
		} else if (event.getEntity() instanceof PullRequest) {
			PullRequest pullRequest = (PullRequest) event.getEntity();
			Query query = getSession().createQuery("delete from PullRequest where type=:type and source=:source");
			query.setParameter("project", pullRequest.getTargetProject());
			query.setParameter("type", Task.TYPE_PULLREQUEST);
			query.setParameter("source", pullRequest.getNumberStr());
			query.executeUpdate();
		}
	}

	@Sessional
	@Override
	public Collection<Task> findAll(PullRequest request, User user, String description) {
		EntityCriteria<Task> criteria = EntityCriteria.of(Task.class);
		criteria.add(Restrictions.eq("project", request.getTargetProject()))
				.add(Restrictions.eq("type", Task.TYPE_PULLREQUEST))
				.add(Restrictions.eq("source", request.getNumberStr()));
		if (user != null)
			criteria.add(Restrictions.eq("user", user));
		if (description != null)
			criteria.add(Restrictions.eq("description", description));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<Task> findAll(Issue issue, User user, String description) {
		EntityCriteria<Task> criteria = EntityCriteria.of(Task.class);
		criteria.add(Restrictions.eq("project", issue.getProject()))
				.add(Restrictions.eq("type", Task.TYPE_ISSUE))
				.add(Restrictions.eq("source", issue.getNumberStr()));
		if (user != null)
			criteria.add(Restrictions.eq("user", user));
		if (description != null)
			criteria.add(Restrictions.eq("description", description));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public Task addTask(PullRequest request, User user, String description) {
		Task task = new Task();
		task.setDescription(description);
		task.setUser(user);
		task.setProject(request.getTargetProject());
		task.setType(Task.TYPE_PULLREQUEST);
		task.setSource(request.getNumberStr());
		save(task);
		return task;
	}

	@Transactional
	@Override
	public Task addTask(Issue issue, User user, String description) {
		Task task = new Task();
		task.setDescription(description);
		task.setUser(user);
		task.setProject(issue.getProject());
		task.setType(Task.TYPE_ISSUE);
		task.setSource(issue.getNumberStr());
		save(task);
		return task;
	}

	@Transactional
	@Override
	public void deleteTasks(PullRequest request, User user, String description) {
		for (Task task: findAll(request, user, description))
			delete(task);
	}

	@Transactional
	@Override
	public void deleteTasks(Issue issue, User user, String description) {
		for (Task task: findAll(issue, user, description))
			delete(task);
	}
	
}