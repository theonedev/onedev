package io.onedev.server.service;

import io.onedev.server.model.IssueVote;

public interface IssueVoteService extends EntityService<IssueVote> {

    void create(IssueVote vote);
	
}
