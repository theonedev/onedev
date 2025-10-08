package io.onedev.server.buildspec.job;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.param.instance.ParamInstances;
import io.onedev.server.buildspec.param.instance.ParamMap;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;

public class TriggerMatch implements Serializable {

	private static final long serialVersionUID = 1L;
	
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
			return OneDev.getInstance(PullRequestService.class).load(requestId);
		else 
			return null;
	}

	@Nullable
	public Issue getIssue() {
		if (issueId != null)
			return OneDev.getInstance(IssueService.class).load(issueId);
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

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != getClass())
			return false;
		TriggerMatch otherTriggerMatch = (TriggerMatch) other;
		return new EqualsBuilder()
				.append(refName, otherTriggerMatch.refName)
				.append(requestId, otherTriggerMatch.requestId)
				.append(issueId, otherTriggerMatch.issueId)
				.append(paramMatrix, otherTriggerMatch.paramMatrix)
				.append(excludeParamMaps, otherTriggerMatch.excludeParamMaps)
				.append(reason, otherTriggerMatch.reason)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(refName)
				.append(requestId)
				.append(issueId)
				.append(paramMatrix)
				.append(excludeParamMaps)
				.append(reason)
				.toHashCode();
	}
	
}
