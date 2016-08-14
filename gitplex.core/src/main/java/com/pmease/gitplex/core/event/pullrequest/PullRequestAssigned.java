package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="assigned", icon="fa fa-user")
public class PullRequestAssigned extends PullRequestStatusChangeEvent {

	public PullRequestAssigned(PullRequest request, Account user, String note) {
		super(request, user, new Date(), note);
	}

}
