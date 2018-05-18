package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.IssueQuerySettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultIssueQuerySettingManager extends AbstractEntityManager<IssueQuerySetting> 
		implements IssueQuerySettingManager {

	@Inject
	public DefaultIssueQuerySettingManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public IssueQuerySetting find(Project project, User user) {
		EntityCriteria<IssueQuerySetting> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		return find(criteria);
	}

	@Transactional
	@Override
	public void save(IssueQuerySetting setting) {
		setting.getUserQueryWatches().keySet().retainAll(setting.getUserQueries().keySet());
		setting.getProjectQueryWatches().keySet().retainAll(setting.getProject().getIssueWorkflow().getQueries().keySet());
		if (setting.getProjectQueryWatches().isEmpty() && setting.getUserQueries().isEmpty()) {
			if (!setting.isNew())
				delete(setting);
		} else {
			super.save(setting);
		}
	}

}
