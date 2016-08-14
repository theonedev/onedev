package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="integrated", icon="fa fa-check")
public class PullRequestIntegrated extends PullRequestStatusChangeEvent {

	public PullRequestIntegrated(PullRequest request, Account user, String note) {
		super(request, user, request.getCloseInfo().getCloseDate(), note);
	}

}
