package com.pmease.gitplex.core.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.CommitCommentManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class CommentAwareChange extends RevAwareChange {
	
	private final Map<Integer, List<CommitComment>> oldComments;
	
	private final Map<Integer, List<CommitComment>> newComments;
	
	private final CommitComment concernedComment;
	
	public CommentAwareChange(Repository repo, RevAwareChange change, boolean enableOldComments, 
			boolean enableNewComments, @Nullable CommitComment concernedComment) {
		super(change);

		this.concernedComment = concernedComment;
		
		if (enableOldComments && getOldPath() != null) {
			oldComments = new HashMap<>();
			CommitCommentManager manager = GitPlex.getInstance(CommitCommentManager.class);
			for (CommitComment comment: manager.findByCommitAndFile(repo, getOldRevision(), getOldPath())) {
				CommentPosition position = comment.getPosition();
				if (position != null && position.getLineNo() != null
						&& position.getFilePath().equals(getOldPath())) {
					List<CommitComment> lineComments = oldComments.get(position.getLineNo());
					if (lineComments == null) {
						lineComments = new ArrayList<>();
						oldComments.put(position.getLineNo(), lineComments);
					}
					lineComments.add(comment);
				}
			}
		} else {
			oldComments = null;
		}

		if (enableNewComments && getNewPath() != null) {
			newComments = new HashMap<>();
			CommitCommentManager manager = GitPlex.getInstance(CommitCommentManager.class);
			for (CommitComment comment: manager.findByCommitAndFile(repo, getNewRevision(), getNewPath())) {
				CommentPosition position = comment.getPosition();
				if (position != null && position.getLineNo() != null
						&& position.getFilePath().equals(getOldPath())) {
					List<CommitComment> lineComments = newComments.get(position.getLineNo());
					if (lineComments == null) {
						lineComments = new ArrayList<>();
						newComments.put(position.getLineNo(), lineComments);
					}
					lineComments.add(comment);
				}
			}
		} else {
			newComments = null;
		}
	}

	public CommitComment getConcernedComment() {
		return concernedComment;
	}

	public Map<Integer, List<CommitComment>> getOldComments() {
		return oldComments;
	}

	public Map<Integer, List<CommitComment>> getNewComments() {
		return newComments;
	}

	public void saveComment(CommitComment comment) {
		boolean isNew = comment.isNew();
		GitPlex.getInstance(Dao.class).persist(comment);
		
		if (isNew) {
			if (comment.getCommit().equals(getOldRevision())) {
				addComment(Preconditions.checkNotNull(oldComments), comment);
			} else if (comment.getCommit().equals(getNewRevision())) {
				addComment(Preconditions.checkNotNull(newComments), comment);
			} else {
				throw new IllegalStateException();
			}
		}
	}
	
	public boolean contains(CommitComment comment) {
		if (oldComments != null) {
			for (List<CommitComment> lineComments: oldComments.values()) {
				for (CommitComment lineComment: lineComments) {
					if (lineComment.equals(comment))
						return true;
				}
			}
		}
		if (newComments != null) {
			for (List<CommitComment> lineComments: newComments.values()) {
				for (CommitComment lineComment: lineComments) {
					if (lineComment.equals(comment))
						return true;
				}
			}
		}
		return false;
	}
	
	public void removeComment(CommitComment comment) {
		GitPlex.getInstance(Dao.class).remove(comment);
		
		if (comment.getCommit().equals(getOldRevision())) {
			removeComment(Preconditions.checkNotNull(oldComments), comment);
		} else if (comment.getCommit().equals(getNewRevision())) {
			removeComment(Preconditions.checkNotNull(newComments), comment);
		} else {
			throw new IllegalStateException();
		}
	}

	private void removeComment(Map<Integer, List<CommitComment>> comments, CommitComment comment) {
		for (Iterator<Entry<Integer, List<CommitComment>>> it = comments.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, List<CommitComment>> entry = it.next();
			entry.getValue().remove(comment);
			if (entry.getValue().isEmpty())
				it.remove();
		}
	}
	
	private void addComment(Map<Integer, List<CommitComment>> comments, CommitComment comment) {
		int lineNo = comment.getPosition().getLineNo();
		List<CommitComment> lineComments = comments.get(lineNo);
		if (lineComments == null) {
			lineComments = new ArrayList<>();
			comments.put(lineNo, lineComments);
		}
		lineComments.add(comment);
	}
	
}
