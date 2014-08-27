package com.pmease.gitplex.core.comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;

@SuppressWarnings("serial")
public class CommentAwareChange extends RevAwareChange {
	
	private final Map<String, Date> commits;
	
	private Map<Integer, List<CommitComment>> oldComments;
	
	private Map<Integer, List<CommitComment>> newComments;
	
	public CommentAwareChange(RevAwareChange change, LinkedHashMap<String, Date> commits, 
			CommentLoader commentLoader, BlobLoader blobLoader) {
		super(change);
		
		Map<String, Map<String, List<CommitComment>>> comments = new HashMap<>();
		Map<BlobInfo, List<String>> blobs = new HashMap<>();
	
		this.commits = commits;
		
		List<String> commitHashes = new ArrayList<>(commits.keySet());
		int end = -1;
		if (change.getNewPath() != null) {
			end = commitHashes.indexOf(change.getNewRevision());
			if (end != -1 && getBlob(blobs, change.getNewBlobInfo(), blobLoader).isEmpty())
				end = -1;
		}

		int begin = -1;
		if (change.getOldPath() != null) { 
			begin = commitHashes.indexOf(change.getOldRevision());
			if (begin != -1 && getBlob(blobs, change.getOldBlobInfo(), blobLoader).isEmpty())
				begin = -1;
		}

		if (end != -1) {
			newComments = new HashMap<>();
			
			for (int i=end-1; i>begin; i--) {
				String commit = commitHashes.get(i);
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
				String commit = commitHashes.get(i);
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
	
	public Map<String, Date> getCommits() {
		return commits;
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
	
	public boolean contains(CommitComment comment) {
		if (getOldComments() != null) {
			for (List<CommitComment> lineComments: getOldComments().values()) {
				for (CommitComment each: lineComments) {
					if (each.getId().equals(comment.getId())) 
						return true;
				}
			}
		}
		if (getNewComments() != null) {
			for (List<CommitComment> lineComments: getNewComments().values()) {
				for (CommitComment each: lineComments) {
					if (each.getId().equals(comment.getId())) 
						return true;
				}
			}
		}
		return false;
	}

	private static class CommentKey extends Pair<String, CommentPosition> {
		
		public CommentKey(String commit, CommentPosition position) {
			super(commit, position);
		}
		
	}
}
