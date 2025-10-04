package io.onedev.server.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.service.IssueVoteService;
import io.onedev.server.service.IssueWatchService;
import io.onedev.server.model.IssueVote;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class DefaultIssueVoteService extends BaseEntityService<IssueVote>
		implements IssueVoteService {

	@Inject
	private IssueWatchService watchService;

	@Transactional
	@Override
	public void create(IssueVote vote) {
		Preconditions.checkState(vote.isNew());
		vote.getIssue().setVoteCount(vote.getIssue().getVoteCount()+1);
		dao.persist(vote);
		watchService.watch(vote.getIssue(), vote.getUser(), true);
	}

	@Transactional
	@Override
	public void delete(IssueVote vote) {
		super.delete(vote);
		vote.getIssue().setVoteCount(vote.getIssue().getVoteCount()-1);
	}

}
