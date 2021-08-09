package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultIssueQueryPersonalizationManager extends BaseEntityManager<IssueQueryPersonalization> 
		implements IssueQueryPersonalizationManager {

	@Inject
	public DefaultIssueQueryPersonalizationManager(Dao dao) {
		super(dao);
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
	public void save(IssueQueryPersonalization personalization) {
		Collection<String> retainNames = new HashSet<>();
		retainNames.addAll(personalization.getQueries().stream()
				.map(it->NamedQuery.PERSONAL_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		retainNames.addAll(personalization.getProject().getIssueSetting().getNamedQueries(true).stream()
				.map(it->NamedQuery.GLOBAL_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		personalization.getQueryWatchSupport().getQueryWatches().keySet().retainAll(retainNames);
		
		if (personalization.getQueryWatchSupport().getQueryWatches().isEmpty() && personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			super.save(personalization);
		}
	}

}
