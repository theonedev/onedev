package io.onedev.server.entitymanager;

import io.onedev.server.model.CodeCommentQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentQuerySettingManager extends EntityManager<CodeCommentQuerySetting> {
	
	CodeCommentQuerySetting find(Project project, User user);
	
}
