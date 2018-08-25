package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.CodeCommentQuerySettingManager;
import io.onedev.server.model.CodeCommentQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultCodeCommentQuerySettingManager extends AbstractEntityManager<CodeCommentQuerySetting> 
		implements CodeCommentQuerySettingManager {

	@Inject
	public DefaultCodeCommentQuerySettingManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public CodeCommentQuerySetting find(Project project, User user) {
		EntityCriteria<CodeCommentQuerySetting> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		return find(criteria);
	}

	@Transactional
	@Override
	public void save(CodeCommentQuerySetting setting) {
		if (setting.getUserQueries().isEmpty()) {
			if (!setting.isNew())
				delete(setting);
		} else {
			super.save(setting);
		}
	}

}
