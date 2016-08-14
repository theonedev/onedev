package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="deleted source branch", icon="fa fa-times")
public class SourceBranchDeleted extends PullRequestStatusChangeEvent {

	public SourceBranchDeleted(PullRequest request, Account user, String note) {
		super(request, user, new Date(), note);
	}

}
