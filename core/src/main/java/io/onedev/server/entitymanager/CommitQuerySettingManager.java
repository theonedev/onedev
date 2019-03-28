package io.onedev.server.entitymanager;

import io.onedev.server.model.CommitQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface CommitQuerySettingManager extends EntityManager<CommitQuerySetting> {
	
	CommitQuerySetting find(Project project, User user);
	
}
