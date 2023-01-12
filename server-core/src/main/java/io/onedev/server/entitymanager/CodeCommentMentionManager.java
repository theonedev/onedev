package io.onedev.server.entitymanager;

import io.onedev.server.model.*;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentMentionManager extends EntityManager<CodeCommentMention> {

	void mention(CodeComment comment, User user);
		
}
