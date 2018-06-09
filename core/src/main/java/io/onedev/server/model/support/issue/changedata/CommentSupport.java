package io.onedev.server.model.support.issue.changedata;

import java.io.Serializable;

public interface CommentSupport extends Serializable {
	
	String getComment();
	
	void setComment(String comment);
	
}
