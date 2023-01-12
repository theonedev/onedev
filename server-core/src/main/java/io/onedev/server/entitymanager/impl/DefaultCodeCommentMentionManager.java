package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.CodeCommentMentionManager;
import io.onedev.server.entitymanager.IssueMentionManager;
import io.onedev.server.model.*;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultCodeCommentMentionManager extends BaseEntityManager<CodeCommentMention>
		implements CodeCommentMentionManager {

	@Inject
	public DefaultCodeCommentMentionManager(Dao dao) {
		super(dao);
	}

	@Override
	public void mention(CodeComment comment, User user) {
		if (comment.getMentions().stream().noneMatch(it->it.getUser().equals(user))) {
			CodeCommentMention mention = new CodeCommentMention();
			mention.setComment(comment);
			mention.setUser(user);
			save(mention);
		}
	}
	
}
