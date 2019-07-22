package io.onedev.server.entitymanager;

import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface BuildQuerySettingManager extends EntityManager<BuildQuerySetting> {
	
	BuildQuerySetting find(Project project, User user);
	
}
