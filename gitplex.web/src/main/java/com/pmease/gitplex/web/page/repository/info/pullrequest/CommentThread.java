package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;

@SuppressWarnings("serial")
public class CommentThread implements Comparable<CommentThread>, Serializable {
	
	private final CommentPosition position;

	private final List<CommitComment> comments;

	public CommentThread(@Nullable CommentPosition position, List<CommitComment> comments) {
		this.position = position;
		this.comments = comments;
	}

	public CommentPosition getPosition() {
		return position;
	}

	public List<CommitComment> getComments() {
		return comments;
	}
	
	public CommitComment getFirstComment() {
		return comments.get(0);
	}
	
	public CommitComment getLastComment() {
		return comments.get(comments.size()-1);
	}

	@Override
	public int compareTo(CommentThread o) {
		return o.getLastComment().getId().compareTo(getLastComment().getId());
	}
}
