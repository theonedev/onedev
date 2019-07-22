package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestQuerySetting;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestQuerySettingManager extends EntityManager<PullRequestQuerySetting> {
	
	PullRequestQuerySetting find(Project project, User user);
	
}
