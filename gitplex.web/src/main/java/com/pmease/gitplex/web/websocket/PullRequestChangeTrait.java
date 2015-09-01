package com.pmease.gitplex.web.websocket;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.pmease.commons.wicket.websocket.WebSocketTrait;
import com.pmease.gitplex.core.model.PullRequest;

public class PullRequestChangeTrait implements WebSocketTrait {
	
	private static final long serialVersionUID = 1L;

	public Long requestId;
	
	@Nullable
	public PullRequest.Event requestEvent;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())  
			return false;  
		PullRequestChangeTrait other = (PullRequestChangeTrait) obj;  
	    return Objects.equal(requestId, other.requestId);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(requestId)
			.toHashCode();
	}

	@Override
	public boolean is(WebSocketTrait trait) {
		if (trait instanceof PullRequestChangeTrait) {
			PullRequestChangeTrait pullRequestChangeTrait = (PullRequestChangeTrait) trait;
			if (requestId.equals(pullRequestChangeTrait.requestId)) {
				if (requestEvent == null) 
					return pullRequestChangeTrait.requestEvent == null;
				else 
					return pullRequestChangeTrait.requestEvent == null || requestEvent == pullRequestChangeTrait.requestEvent;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}		
	
}