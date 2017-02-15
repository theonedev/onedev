package com.gitplex.server.web.websocket;

import com.gitplex.server.web.websocket.WebSocketRegion;

public class CodeCommentChangedRegion implements WebSocketRegion {

	private final Long commentId;
	
	public CodeCommentChangedRegion(Long commentId) {
		this.commentId = commentId;
	}
	
	@Override
	public boolean contains(WebSocketRegion region) {
		if (region instanceof CodeCommentChangedRegion) {
			CodeCommentChangedRegion codeCommentChangedRegion = (CodeCommentChangedRegion) region;
			return commentId.equals(codeCommentChangedRegion.commentId);
		} else {
			return false;
		}
	}

}
