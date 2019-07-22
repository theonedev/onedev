package io.onedev.server.util;

import java.io.Serializable;

public interface CommentSupport extends Serializable {
	
	String getComment();
	
	void setComment(String comment);
	
}
