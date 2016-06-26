package com.pmease.gitplex.web.websocket;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.pmease.commons.wicket.websocket.WebSocketTrait;

public class CodeCommentChangeTrait implements WebSocketTrait {
	
	private static final long serialVersionUID = 1L;

	public Long commentId;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())  
			return false;  
		CodeCommentChangeTrait other = (CodeCommentChangeTrait) obj;  
	    return Objects.equal(commentId, other.commentId);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(commentId)
			.toHashCode();
	}

	@Override
	public boolean is(WebSocketTrait trait) {
		return trait.equals(this);
	}		
	
}