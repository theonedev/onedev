package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultPendingVoteManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.PendingVote;
import com.pmease.gitop.core.model.User;

@ImplementedBy(DefaultPendingVoteManager.class)
public interface PendingVoteManager extends GenericDao<PendingVote> {
	
	PendingVote find(User reviewer, MergeRequest request);

}
