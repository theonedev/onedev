package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.PullRequestQueryPersonalizationManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestQueryPersonalization;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Singleton
public class DefaultPullRequestQueryPersonalizationManager extends BaseEntityManager<PullRequestQueryPersonalization> 
		implements PullRequestQueryPersonalizationManager {

	@Inject
	public DefaultPullRequestQueryPersonalizationManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public PullRequestQueryPersonalization find(Project project, User user) {
		EntityCriteria<PullRequestQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	private void createOrUpdate(PullRequestQueryPersonalization personalization) {
		Collection<String> retainNames = new HashSet<>();
		retainNames.addAll(personalization.getQueries().stream()
				.map(it->NamedQuery.PERSONAL_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		retainNames.addAll(personalization.getProject().getNamedPullRequestQueries().stream()
				.map(it->NamedQuery.COMMON_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		personalization.getQueryWatchSupport().getQueryWatches().keySet().retainAll(retainNames);
		
		if (personalization.getQueryWatchSupport().getQueryWatches().isEmpty() && personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}

	@Transactional
	@Override
	public void create(PullRequestQueryPersonalization personalization) {
		Preconditions.checkState(personalization.isNew());
		createOrUpdate(personalization);
	}

	@Transactional
	@Override
	public void update(PullRequestQueryPersonalization personalization) {
		Preconditions.checkState(!personalization.isNew());
		createOrUpdate(personalization);
	}
}
