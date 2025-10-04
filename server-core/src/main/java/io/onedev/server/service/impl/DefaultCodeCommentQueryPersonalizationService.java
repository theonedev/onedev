package io.onedev.server.service.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.model.CodeCommentQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.CodeCommentQueryPersonalizationService;

@Singleton
public class DefaultCodeCommentQueryPersonalizationService extends BaseEntityService<CodeCommentQueryPersonalization>
		implements CodeCommentQueryPersonalizationService {

	@Sessional
	@Override
	public CodeCommentQueryPersonalization find(Project project, User user) {
		EntityCriteria<CodeCommentQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void createOrUpdate(CodeCommentQueryPersonalization personalization) {
		if (personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}
	
}
