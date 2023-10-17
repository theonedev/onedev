package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.BuildQueryPersonalizationManager;
import io.onedev.server.model.BuildQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultBuildQueryPersonalizationManager extends BaseEntityManager<BuildQueryPersonalization> 
		implements BuildQueryPersonalizationManager {

	@Inject
	public DefaultBuildQueryPersonalizationManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public BuildQueryPersonalization find(Project project, User user) {
		EntityCriteria<BuildQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	private void createOrUpdate(BuildQueryPersonalization personalization) {
		Collection<String> retainNames = new HashSet<>();
		retainNames.addAll(personalization.getQueries().stream()
				.map(it->NamedQuery.PERSONAL_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		retainNames.addAll(personalization.getProject().getNamedBuildQueries().stream()
				.map(it->NamedQuery.COMMON_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		personalization.getQuerySubscriptionSupport().getQuerySubscriptions().retainAll(retainNames);
		
		if (personalization.getQuerySubscriptionSupport().getQuerySubscriptions().isEmpty() && personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}

	@Transactional
	@Override
	public void create(BuildQueryPersonalization personalization) {
		Preconditions.checkState(personalization.isNew());
		createOrUpdate(personalization);
	}

	@Transactional
	@Override
	public void update(BuildQueryPersonalization personalization) {
		Preconditions.checkState(!personalization.isNew());
		createOrUpdate(personalization);
	}
}
