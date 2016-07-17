package com.pmease.gitplex.web.websocket;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.pmease.commons.wicket.websocket.WebSocketTrait;

public class PullRequestChangeTrait implements WebSocketTrait {
	
	private static final long serialVersionUID = 1L;

	public Long requestId;
	
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
			return requestId.equals(pullRequestChangeTrait.requestId);
		} else {
			return false;
		}
	}		
	
}