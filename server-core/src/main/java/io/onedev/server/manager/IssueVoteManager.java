package io.onedev.server.manager;

import io.onedev.server.model.IssueVote;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueVoteManager extends EntityManager<IssueVote> {

    void create(IssueVote vote);
	
}
