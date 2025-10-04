package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestCommentReaction;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PullRequestCommentReactionService;

@Singleton
public class DefaultPullRequestCommentReactionService extends BaseEntityService<PullRequestCommentReaction>
        implements PullRequestCommentReactionService {

    @Transactional
    @Override
    public void create(PullRequestCommentReaction reaction) {
        Preconditions.checkState(reaction.isNew());
        dao.persist(reaction);
    }

    @Transactional
    @Override
    public void toggleEmoji(User user, PullRequestComment comment, String emoji) {
        var reaction = comment.getReactions().stream()
                .filter(r -> r.getUser().equals(user) && r.getEmoji().equals(emoji))
                .findFirst()
                .orElse(null);
        if (reaction == null) {
            reaction = new PullRequestCommentReaction();
            reaction.setUser(user);
            reaction.setComment(comment);
            reaction.setEmoji(emoji);
            create(reaction);
            comment.getReactions().add(reaction);
        } else {
            comment.getReactions().remove(reaction);
            dao.remove(reaction);
        }
    }

} 