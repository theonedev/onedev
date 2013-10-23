package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;

@SuppressWarnings("serial")
@Editable
public class ApprovedBySpecifiedUser extends AbstractGateKeeper {

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
    public CheckResult check(MergeRequest request) {
        UserManager userManager = AppLoader.getInstance(UserManager.class);
        User user = userManager.load(getUserId());

        Vote.Result result = user.checkVoteSince(request.getBaseUpdate());
        if (result == null) {
            request.inviteToVote(Sets.newHashSet(user), 1);
            return pending("To be approved by user '" + user.getName() + "'.",
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
