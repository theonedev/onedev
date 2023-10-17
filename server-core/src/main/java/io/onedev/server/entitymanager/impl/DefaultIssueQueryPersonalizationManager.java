package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.ProjectScope;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import static io.onedev.server.model.AbstractEntity.PROP_ID;
import static io.onedev.server.model.IssueQueryPersonalization.PROP_PROJECT;

@Singleton
public class DefaultIssueQueryPersonalizationManager extends BaseEntityManager<IssueQueryPersonalization> 
		implements IssueQueryPersonalizationManager {

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultIssueQueryPersonalizationManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@Sessional
	@Override
	public IssueQueryPersonalization find(Project project, User user) {
		EntityCriteria<IssueQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void create(IssueQueryPersonalization personalization) {
		Preconditions.checkState(personalization.isNew());
		createrOrUpdate(personalization);
	}

	@Transactional
	@Override
	public void update(IssueQueryPersonalization personalization) {
		Preconditions.checkState(!personalization.isNew());
		createrOrUpdate(personalization);
	}
	
	@Sessional
	@Override
	public Collection<IssueQueryPersonalization> query(ProjectScope projectScope) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		var criteriaQuery = builder.createQuery(IssueQueryPersonalization.class);
		var root = criteriaQuery.from(IssueQueryPersonalization.class);
		
		var projectId = projectScope.getProject().getId();
		Collection<Long> checkIds = new HashSet<>();
		if (projectScope.isInherited()) {
			for (Project ancestor: projectScope.getProject().getAncestors())
				checkIds.add(ancestor.getId());
		}
		if (projectScope.isRecursive()) 
			checkIds.addAll(projectManager.getSubtreeIds(projectId));
		else 
			checkIds.add(projectId);
		criteriaQuery.where(root.get(PROP_PROJECT).get(PROP_ID).in(checkIds));
		var query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(Integer.MAX_VALUE);
		return query.getResultList();
	}

	private void createrOrUpdate(IssueQueryPersonalization personalization) {
		Collection<String> retainNames = new HashSet<>();
		retainNames.addAll(personalization.getQueries().stream()
				.map(it->NamedQuery.PERSONAL_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		retainNames.addAll(personalization.getProject().getNamedIssueQueries().stream()
				.map(it->NamedQuery.COMMON_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		personalization.getQueryWatchSupport().getQueryWatches().keySet().retainAll(retainNames);

		if (personalization.getQueryWatchSupport().getQueryWatches().isEmpty() && personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}
	
}
