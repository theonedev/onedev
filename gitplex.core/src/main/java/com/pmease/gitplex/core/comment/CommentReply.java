package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.Date;

import com.pmease.gitplex.core.model.User;

public interface CommentReply extends Serializable {
	Comment getComment();
	
	User getUser();
	
	Date getDate();
	
	String getContent();
	
	void saveContent(String content);
	
	void delete();
}
