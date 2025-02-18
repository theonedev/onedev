package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.IssueReactionManager;
import io.onedev.server.model.IssueReaction;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueReactionManager extends BaseEntityManager<IssueReaction> implements IssueReactionManager {

    @Inject
    public DefaultIssueReactionManager(Dao dao) {
        super(dao);
    }
    
	@Transactional
    @Override
    public void create(IssueReaction reaction) {
		Preconditions.checkState(reaction.isNew());
		dao.persist(reaction);
    }

    @Transactional
    @Override
    public void toggleEmoji(User user, Issue issue, String emoji) {
        var reaction = issue.getReactions().stream()
                .filter(r -> r.getUser().equals(user) && r.getEmoji().equals(emoji))
                .findFirst()
                .orElse(null);
                
        switch (emoji) {
            case "👍":
                issue.setThumbsUpCount(issue.getThumbsUpCount() + (reaction == null ? 1 : -1));
                break;
            case "👎":
                issue.setThumbsDownCount(issue.getThumbsDownCount() + (reaction == null ? 1 : -1));
                break;
            case "😄":
                issue.setSmileCount(issue.getSmileCount() + (reaction == null ? 1 : -1));
                break;
            case "🎉":
                issue.setTadaCount(issue.getTadaCount() + (reaction == null ? 1 : -1));
                break;
            case "😕":
                issue.setConfusedCount(issue.getConfusedCount() + (reaction == null ? 1 : -1));
                break;
            case "❤️":
                issue.setHeartCount(issue.getHeartCount() + (reaction == null ? 1 : -1));
                break;
            case "🚀":
                issue.setRocketCount(issue.getRocketCount() + (reaction == null ? 1 : -1));
                break;
            case "👀":
                issue.setEyesCount(issue.getEyesCount() + (reaction == null ? 1 : -1));
                break;
        }
                
        if (reaction != null) {
            issue.getReactions().remove(reaction);
            dao.remove(reaction);
        } else {
            reaction = new IssueReaction();
            reaction.setUser(user);
            reaction.setIssue(issue);
            reaction.setEmoji(emoji);
            create(reaction);
            issue.getReactions().add(reaction);
        }
    }
}