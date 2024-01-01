package io.onedev.server.buildspec.job;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.param.instance.ParamInstances;
import io.onedev.server.buildspec.param.instance.ParamMap;
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
	
	private final List<ParamInstances> paramMatrix;
	
	private final List<? extends ParamMap> excludeParamMaps;
	
	private final String reason;
	
	public TriggerMatch(String refName, @Nullable PullRequest request, @Nullable Issue issue,
                        List<ParamInstances> paramMatrix, List<? extends ParamMap> excludeParamMaps, 
						String reason) {
		this.refName = refName;
		requestId = PullRequest.idOf(request);
		issueId = Issue.idOf(issue);
		this.paramMatrix = paramMatrix;
		this.excludeParamMaps = excludeParamMaps;
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

	public List<ParamInstances> getParamMatrix() {
		return paramMatrix;
	}
	
	public List<? extends ParamMap> getExcludeParamMaps() {
		return excludeParamMaps;
	}

	public String getReason() {
		return reason;
	}
}
