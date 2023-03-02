package io.onedev.server.entitymanager;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentMention;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentMentionManager extends EntityManager<CodeCommentMention> {

	void mention(CodeComment comment, User user);

}
