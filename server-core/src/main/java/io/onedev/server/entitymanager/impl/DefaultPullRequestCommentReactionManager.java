package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.PullRequestCommentReactionManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestCommentReaction;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestCommentReactionManager extends BaseEntityManager<PullRequestCommentReaction> 
        implements PullRequestCommentReactionManager {

    @Inject
    public DefaultPullRequestCommentReactionManager(Dao dao) {
        super(dao);
    }
    
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