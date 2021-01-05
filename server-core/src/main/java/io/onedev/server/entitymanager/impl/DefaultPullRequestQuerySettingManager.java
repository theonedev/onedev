package io.onedev.server.entitymanager.impl;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.PullRequestQuerySettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestQuerySetting;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultPullRequestQuerySettingManager extends BaseEntityManager<PullRequestQuerySetting> 
		implements PullRequestQuerySettingManager {

	@Inject
	public DefaultPullRequestQuerySettingManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public PullRequestQuerySetting find(Project project, User user) {
		EntityCriteria<PullRequestQuerySetting> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void save(PullRequestQuerySetting setting) {
		setting.getQueryWatchSupport().getUserQueryWatches().keySet().retainAll(
				setting.getUserQueries().stream().map(it->it.getName()).collect(Collectors.toSet()));
		setting.getQueryWatchSupport().getQueryWatches().keySet().retainAll(
				setting.getProject().getIssueSetting().getNamedQueries(true).stream().map(it->it.getName()).collect(Collectors.toSet()));
		if (setting.getQueryWatchSupport().getQueryWatches().isEmpty() && setting.getUserQueries().isEmpty()) {
			if (!setting.isNew())
				delete(setting);
		} else {
			super.save(setting);
		}
	}

}
