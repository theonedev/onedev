package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.IssueWork;
import io.onedev.server.model.IssueWorkReaction;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.IssueWorkReactionService;

@Singleton
public class DefaultIssueWorkReactionService extends BaseEntityService<IssueWorkReaction>
		implements IssueWorkReactionService {

	@Transactional
	@Override
	public void create(IssueWorkReaction reaction) {
		Preconditions.checkState(reaction.isNew());
		dao.persist(reaction);
	}

	@Transactional
	@Override
	public void toggleEmoji(User user, IssueWork work, String emoji) {
		var reaction = work.getReactions().stream()
				.filter(r -> r.getUser().equals(user) && r.getEmoji().equals(emoji))
				.findFirst()
				.orElse(null);
		if (reaction == null) {
			reaction = new IssueWorkReaction();
			reaction.setWork(work);
			reaction.setUser(user);
			reaction.setEmoji(emoji);
			create(reaction);
			work.getReactions().add(reaction);
		} else {
			work.getReactions().remove(reaction);
			dao.remove(reaction);
		}
	}

}
