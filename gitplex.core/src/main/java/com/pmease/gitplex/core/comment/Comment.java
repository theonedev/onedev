package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

public interface Comment extends Serializable {
	Long getId();
	
	User getUser();
	
	Date getDate();
	
	String getContent();
	
	void saveContent(String content);
	
	boolean isResolved();
	
	void resolve(boolean resolved);
	
	Repository getRepository();
	
	void delete();
	
	Collection<? extends CommentReply> getReplies();
	
	CommentReply addReply(String content);
	
	/**
	 * Get last visit date of this comment for current user.
	 * 
	 * @return
	 * 			last visit date of this comment for current user, or <tt>null</tt>
	 * 			if current user is anonymous or has never visited the comment
	 */
	@Nullable
	Date getLastVisitDate();
}
