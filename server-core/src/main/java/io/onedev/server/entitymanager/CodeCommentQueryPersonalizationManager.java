package io.onedev.server.entitymanager;

import io.onedev.server.model.CodeCommentQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentQueryPersonalizationManager extends EntityManager<CodeCommentQueryPersonalization> {
	
	CodeCommentQueryPersonalization find(Project project, User user);

    void create(CodeCommentQueryPersonalization personalization);

	void update(CodeCommentQueryPersonalization personalization);
	
}
