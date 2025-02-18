package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueReaction;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueReactionManager extends EntityManager<IssueReaction> {

    void create(IssueReaction reaction);
    
    void toggleEmoji(User user, Issue issue, String emoji);

}
