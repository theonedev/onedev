package io.onedev.server.buildspec.job;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

import javax.annotation.Nullable;
import java.util.List;

public class TriggerMatch {
	
	private final String refName;
	
	private final Long requestId;
	
	private final Long issueId;
	
	private final List<ParamSupply> params;
	
	private final String reason;
	
	public TriggerMatch(String refName, @Nullable PullRequest request, @Nullable Issue issue, 
						List<ParamSupply> params, String reason) {
		this.refName = refName;
		requestId = PullRequest.idOf(request);
		issueId = Issue.idOf(issue);
		this.params = params;
		this.reason = reason;
	}

	public String getRefName() {
		return refName;
	}

	@Nullable
	public PullRequest getRequest() {
		if (requestId != null)
			return OneDev.getInstance(PullRequestManager.class).load(requestId);
		else 
			return null;
	}

	@Nullable
	public Issue getIssue() {
		if (issueId != null)
			return OneDev.getInstance(IssueManager.class).load(issueId);
		else
			return null;
	}
	
	public List<ParamSupply> getParams() {
		return params;
	}

	public String getReason() {
		return reason;
	}
}
