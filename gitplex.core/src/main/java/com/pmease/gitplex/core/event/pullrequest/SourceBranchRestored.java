package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="restored source branch", icon="fa fa-repeat")
public class SourceBranchRestored extends PullRequestStatusChangeEvent {

	public SourceBranchRestored(PullRequest request, Account user, String note) {
		super(request, user, note);
	}

}
