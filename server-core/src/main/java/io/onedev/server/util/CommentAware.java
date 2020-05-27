package io.onedev.server.util;

import java.io.Serializable;

public interface CommentAware extends Serializable {
	
	String getComment();
	
	void setComment(String comment);
	
}
