package io.onedev.server.buildspec.job;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.PullRequest;

import javax.annotation.Nullable;
import java.util.List;

public class TriggerMatch {
	
	private final String refName;
	
	private final Long requestId;
	
	private final List<ParamSupply> params;
	
	private final String reason;
	
	public TriggerMatch(String refName, @Nullable PullRequest request, List<ParamSupply> params, String reason) {
		this.refName = refName;
		requestId = PullRequest.idOf(request);
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

	public List<ParamSupply> getParams() {
		return params;
	}

	public String getReason() {
		return reason;
	}
}
