package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultVoteInvitationManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.VoteInvitation;
import com.pmease.gitop.core.model.User;

@ImplementedBy(DefaultVoteInvitationManager.class)
public interface VoteInvitationManager extends GenericDao<VoteInvitation> {
	
	VoteInvitation find(User reviewer, MergeRequest request);

}
