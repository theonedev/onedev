package io.onedev.server.workspace;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.model.WorkspaceQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.impl.BaseEntityService;

@Singleton
public class DefaultWorkspaceQueryPersonalizationService extends BaseEntityService<WorkspaceQueryPersonalization>
		implements WorkspaceQueryPersonalizationService {

	@Sessional
	@Override
	public WorkspaceQueryPersonalization find(Project project, User user) {
		EntityCriteria<WorkspaceQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void createOrUpdate(WorkspaceQueryPersonalization personalization) {
		Collection<String> retainNames = new HashSet<>();
		retainNames.addAll(personalization.getQueries().stream()
				.map(it -> NamedQuery.PERSONAL_NAME_PREFIX + it.getName()).collect(Collectors.toSet()));
		retainNames.addAll(personalization.getProject().getNamedWorkspaceQueries().stream()
				.map(it -> NamedQuery.COMMON_NAME_PREFIX + it.getName()).collect(Collectors.toSet()));
		personalization.getQuerySubscriptionSupport().getQuerySubscriptions().retainAll(retainNames);

		if (personalization.getQuerySubscriptionSupport().getQuerySubscriptions().isEmpty() && personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}

}
