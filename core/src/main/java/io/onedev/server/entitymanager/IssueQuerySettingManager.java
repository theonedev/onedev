package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueQuerySettingManager extends EntityManager<IssueQuerySetting> {
	
	IssueQuerySetting find(Project project, User user);
	
}
