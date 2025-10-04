package io.onedev.server.service;

import io.onedev.server.model.IssueReaction;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

public interface IssueReactionService extends EntityService<IssueReaction> {

    void create(IssueReaction reaction);
    
    void toggleEmoji(User user, Issue issue, String emoji);

}
