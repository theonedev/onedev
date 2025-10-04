package io.onedev.server.service;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentMention;
import io.onedev.server.model.User;

public interface CodeCommentMentionService extends EntityService<CodeCommentMention> {

	void mention(CodeComment comment, User user);

}
