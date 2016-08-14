package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

@Editable(name="has new commits")
public class PullRequestUpdated extends PullRequestChangeEvent {

	private final PullRequestUpdate update;
	
	public PullRequestUpdated(PullRequestUpdate update) {
		super(update.getRequest(), null, new Date());
		this.update = update;
	}

	public PullRequestUpdate getUpdate() {
		return update;
	}

}
