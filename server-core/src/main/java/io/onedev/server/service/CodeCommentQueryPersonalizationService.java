package io.onedev.server.service;

import io.onedev.server.model.CodeCommentQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public interface CodeCommentQueryPersonalizationService extends EntityService<CodeCommentQueryPersonalization> {
	
	CodeCommentQueryPersonalization find(Project project, User user);

    void createOrUpdate(CodeCommentQueryPersonalization personalization);

}
