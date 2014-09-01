package com.pmease.gitplex.core.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;

@SuppressWarnings("serial")
public class CommentAwareChange extends RevAwareChange {
	
	private Map<Integer, List<CommitComment>> oldComments;
	
	private Map<Integer, List<CommitComment>> newComments;
	
	private final CommitComment concernedComment;
	
	public CommentAwareChange(RevAwareChange change, List<String> commits, 
			CommentLoader commentLoader, BlobLoader blobLoader, 
			CommitComment concernedComment) {
		super(change);
		
		this.concernedComment = concernedComment;
		
		Map<String, Map<String, List<CommitComment>>> comments = new HashMap<>();
		Map<BlobInfo, List<String>> blobs = new HashMap<>();
	
		int end = -1;
		if (change.getNewPath() != null) {
			end = commits.indexOf(change.getNewRevision());
			if (end != -1 && getBlob(blobs, change.getNewBlobInfo(), blobLoader).isEmpty())
				end = -1;
		}

		int begin = -1;
		if (change.getOldPath() != null) { 
			begin = commits.indexOf(change.getOldRevision());
			if (begin != -1 && getBlob(blobs, change.getOldBlobInfo(), blobLoader).isEmpty())
				begin = -1;
		}

		if (end != -1) {
			newComments = new HashMap<>();
			
			for (int i=end-1; i>begin; i--) {
				String commit = commits.get(i);
				Map<String, List<CommitComment>> commentsOnCommit = getCommentsOnCommit(comments, commit, commentLoader);
				BlobInfo blobInfo = null;
				List<CommitComment> commentsOnFile = commentsOnCommit.get(change.getNewPath());
				if (commentsOnFile != null) {
					blobInfo = new BlobInfo(commit, change.getNewPath(), change.getNewMode());
				} else { 
					commentsOnFile = commentsOnCommit.get(change.getOldPath());
					if (commentsOnFile != null)
						blobInfo = new BlobInfo(commit, change.getOldPath(), change.getOldMode());
				}
				
				if (blobInfo != null) {
					List<String> fileContent = getBlob(blobs, blobInfo, blobLoader);
					if (!fileContent.isEmpty()) {
						List<String> newContent = getBlob(blobs, change.getNewBlobInfo(), blobLoader);
						Map<Integer, Integer> lineMap = DiffUtils.mapLines(fileContent, newContent);
						for (CommitComment comment: commentsOnFile) {
							Integer lineNo = lineMap.get(comment.getPosition().getLineNo());
							if (lineNo != null)
								addLineComment(newComments, lineNo, comment);
						}
					}
				}
			}

			List<CommitComment> commentsOnFile = getCommentsOnCommit(comments, change.getNewRevision(), commentLoader).get(change.getNewPath());
			if (commentsOnFile != null) {
				for (CommitComment comment: commentsOnFile)
					addLineComment(newComments, comment.getPosition().getLineNo(), comment);
			}
		}

		if (begin != -1) {
			oldComments = new HashMap<>();
			
			Set<CommentKey> appliedComments = new HashSet<>();
			if (newComments != null) {
				for (List<CommitComment> list: newComments.values()) {
					for (CommitComment comment: list)
						appliedComments.add(new CommentKey(comment.getCommit(), comment.getPosition()));
				}
			}

			for (int i=begin+1; i<(end!=-1?end:commits.size()); i++) {
				String commit = commits.get(i);
				Map<String, List<CommitComment>> commentsOnCommit = getCommentsOnCommit(comments, commit, commentLoader);
				BlobInfo blobInfo = null;
				List<CommitComment> commentsOnFile = commentsOnCommit.get(change.getOldPath());
				if (commentsOnFile != null) {
					blobInfo = new BlobInfo(commit, change.getOldPath(), change.getOldMode());
				} else { 
					commentsOnFile = commentsOnCommit.get(change.getNewPath());
					if (commentsOnFile != null)
						blobInfo = new BlobInfo(commit, change.getNewPath(), change.getNewMode());
				}
				
				if (blobInfo != null) {
					boolean allApplied = true;
					for (CommitComment comment: commentsOnFile) {
						CommentKey key = new CommentKey(comment.getCommit(), comment.getPosition());
						if (!appliedComments.contains(key)) {
							allApplied = false;
							break;
						}
					}
					if (!allApplied) {
						List<String> fileContent = blobLoader.loadBlob(blobInfo);
						if (!fileContent.isEmpty()) {
							List<String> oldContent = getBlob(blobs, change.getOldBlobInfo(), blobLoader);
							Map<Integer, Integer> lineMap = DiffUtils.mapLines(fileContent, oldContent);
							for (CommitComment comment: commentsOnFile) {
								CommentKey key = new CommentKey(comment.getCommit(), comment.getPosition());
								Integer lineNo = lineMap.get(comment.getPosition().getLineNo());
								if (!appliedComments.contains(key) && lineNo != null)
									addLineComment(oldComments, lineNo, comment);
							}
						}
					}
				}
			}

			List<CommitComment> commentsOnFile = getCommentsOnCommit(comments, change.getOldRevision(), commentLoader).get(change.getOldPath());
			if (commentsOnFile != null) {
				for (CommitComment comment: commentsOnFile)
					addLineComment(oldComments, comment.getPosition().getLineNo(), comment);
			}
		}
		
	}
	
	private void addLineComment(Map<Integer, List<CommitComment>> comments, int lineNo, CommitComment comment) {
		List<CommitComment> commentsOnLine = comments.get(lineNo);
		if (commentsOnLine == null) {
			commentsOnLine = new ArrayList<>();
			comments.put(lineNo, commentsOnLine);
		}
		commentsOnLine.add(comment);
	}
	
	private Map<String, List<CommitComment>> getCommentsOnCommit(Map<String, Map<String, List<CommitComment>>> comments, 
			String commit, CommentLoader commentLoader) {
		Map<String, List<CommitComment>> commentsOnCommit = comments.get(commit);
		if (commentsOnCommit == null) {
			commentsOnCommit = new HashMap<>();
			for (CommitComment each: commentLoader.loadComments(commit)) {
				if (each.getPosition() != null && each.getPosition().getLineNo() != null) {
					List<CommitComment> commentsOnFile = commentsOnCommit.get(each.getPosition().getFilePath());
					if (commentsOnFile == null) {
						commentsOnFile = new ArrayList<>();
						commentsOnCommit.put(each.getPosition().getFilePath(), commentsOnFile);
					}
					commentsOnFile.add(each);
				}
			}
			comments.put(commit, commentsOnCommit);
		}
		return commentsOnCommit;
	}

	@Nullable
	public Map<Integer, List<CommitComment>> getOldComments() {
		return oldComments;
	}

	@Nullable
	public Map<Integer, List<CommitComment>> getNewComments() {
		return newComments;
	}
	
	private List<String> getBlob(Map<BlobInfo, List<String>> blobs, BlobInfo blobInfo, BlobLoader blobLoader) {
		List<String> blob = blobs.get(blobInfo);
		if (blob == null) {
			blob = blobLoader.loadBlob(blobInfo);
			if (blob == null)
				blob = new ArrayList<>();
			blobs.put(blobInfo, blob);
		}
		return blob;
	}
	
	public CommitComment getConcernedComment() {
		return concernedComment;
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
	
	public void saveComment(CommitComment comment) {
		boolean isNew = comment.isNew();
		GitPlex.getInstance(Dao.class).persist(comment);
		
		if (isNew) {
			if (oldComments != null && (comment.getCommit().equals(getOldRevision()) || contains(oldComments, comment.getCommit())))
				addComment(oldComments, comment);
			if (newComments != null && (comment.getCommit().equals(getNewRevision()) || contains(newComments, comment.getCommit())))
				addComment(newComments, comment);
		}
	}
	
	private boolean contains(Map<Integer, List<CommitComment>> comments, String commit) {
		for (List<CommitComment> lineComments: comments.values()) {
			for (CommitComment lineComment: lineComments) {
				if (lineComment.getCommit().equals(commit))
					return true;
			}
		}
		return false;
	}
	
	public void removeComment(CommitComment comment) {
		GitPlex.getInstance(Dao.class).remove(comment);

		if (oldComments != null)
			removeComment(oldComments, comment);
		if (newComments != null	)
			removeComment(newComments, comment);
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

	private static class CommentKey extends Pair<String, CommentPosition> {
		
		public CommentKey(String commit, CommentPosition position) {
			super(commit, position);
		}
		
	}

}
