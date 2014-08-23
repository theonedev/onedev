package com.pmease.gitplex.core.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;

public class CommentThread implements Comparable<CommentThread> {
	
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
		return getLastComment().getId().compareTo(o.getLastComment().getId());
	}
	
	public static List<CommentThread> asThreads(List<CommitComment> comments) {
		Map<CommentPosition, List<CommitComment>> threadMap = new HashMap<>(); 
		for (CommitComment comment: comments) {
			List<CommitComment> threadComments = threadMap.get(comment.getPosition());
			if (threadComments == null) {
				threadComments = new ArrayList<>();
				threadMap.put(comment.getPosition(), threadComments);
			}
			threadComments.add(comment);
		}
		
		List<CommentThread> threads = new ArrayList<>();
		for (Map.Entry<CommentPosition, List<CommitComment>> entry: threadMap.entrySet()) 
			threads.add(new CommentThread(entry.getKey(), entry.getValue()));
		
		Collections.sort(threads);
		return threads;
	}
}
