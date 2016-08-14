package com.pmease.gitplex.core.event.mention;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

public class AccountMentionedInPullRequest {

	private final PullRequest request;
	
	private final Account user;
	
	private final String markdown;
	
	public AccountMentionedInPullRequest(PullRequest request, Account user, String markdown) {
		this.request = request;
		this.user = user;
		this.markdown = markdown;
	}

	public PullRequest getRequest() {
		return request;
	}

	public Account getUser() {
		return user;
	}

	public String getMarkdown() {
		return markdown;
	}

}
