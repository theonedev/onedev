package com.turbodev.server.event.pullrequest;

import java.util.Date;

import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="merge preview calculated")
public class PullRequestMergePreviewCalculated extends PullRequestEvent {

	private Date date;
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(request);
		date = new Date();
	}

	@Override
	public User getUser() {
		return null;
	}

	@Override
	public Date getDate() {
		return date;
	}

}
