package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;

@SuppressWarnings("serial")
@Editable(order=200, icon="icon-user", description=
		"This gate keeper will be passed if the commit is approved by specified user.")
public class IfApprovedBySpecifiedUser extends ApprovalGateKeeper {

    private Long userId;

    @Editable
    @NotNull
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public CheckResult doCheck(PullRequest request) {
        UserManager userManager = Gitop.getInstance(UserManager.class);
        User user = userManager.load(getUserId());

        Vote.Result result = user.checkVoteSince(request.getBaseUpdate());
        if (result == null) {
            if (request.getId() != null)
            	Gitop.getInstance(VoteInvitationManager.class).inviteToVote(request, Sets.newHashSet(user), 1);

            String prefix;
    		if (request.getId() == null)
    			prefix = "Not ";
    		else
    			prefix = "To be ";
            return pending(prefix + "approved by user '" + user.getName() + "'.",
                    new CanVoteBySpecifiedUser(user));
        } else if (result.isAccept()) {
            return accepted("Approved by user '" + user.getName() + "'.");
        } else {
            return rejected("Rejected by user '" + user.getName() + "'.");
        }
    }

    @Override
    public Object trim(Object context) {
        UserManager userManager = AppLoader.getInstance(UserManager.class);
        if (userManager.get(getUserId()) == null)
            return null;
        else
            return this;
    }

}
