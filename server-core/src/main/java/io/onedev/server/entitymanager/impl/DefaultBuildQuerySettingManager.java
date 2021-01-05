package io.onedev.server.entitymanager.impl;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.BuildQuerySettingManager;
import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultBuildQuerySettingManager extends BaseEntityManager<BuildQuerySetting> 
		implements BuildQuerySettingManager {

	@Inject
	public DefaultBuildQuerySettingManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public BuildQuerySetting find(Project project, User user) {
		EntityCriteria<BuildQuerySetting> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void save(BuildQuerySetting setting) {
		setting.getQuerySubscriptionSupport().getUserQuerySubscriptions().retainAll(
				setting.getUserQueries().stream().map(it->it.getName()).collect(Collectors.toSet()));
		setting.getQuerySubscriptionSupport().getQuerySubscriptions().retainAll(
				setting.getProject().getBuildSetting().getNamedQueries(true).stream().map(it->it.getName()).collect(Collectors.toSet()));
		if (setting.getQuerySubscriptionSupport().getQuerySubscriptions().isEmpty() && setting.getUserQueries().isEmpty()) {
			if (!setting.isNew())
				delete(setting);
		} else {
			super.save(setting);
		}
	}

}
