package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultVoteManager;
import com.pmease.gitop.core.model.MergeRequestUpdate;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;

@ImplementedBy(DefaultVoteManager.class)
public interface VoteManager extends GenericDao<Vote> {

	Vote lookupVote(User reviewer, MergeRequestUpdate update);

}
