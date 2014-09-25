package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.util.Triple;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class ChangeComments implements Serializable {
	
	private final Map<Integer, List<InlineComment>> oldComments = new HashMap<>();
	
	private final Map<Integer, List<InlineComment>> newComments = new HashMap<>();
	
	public ChangeComments(PullRequest request, RevAwareChange change) {
		List<String> commits = new ArrayList<>();
		commits.add(request.getBaseCommitHash());
		for (PullRequestUpdate update: request.getSortedUpdates())
			commits.add(update.getHeadCommitHash());
		
		Map<String, Map<String, List<InlineComment>>> comments = new HashMap<>();
		Map<BlobInfo, List<String>> blobs = new HashMap<>();
	
		int end = -1;
		if (change.getNewPath() != null) {
			end = commits.indexOf(change.getNewRevision());
			if (end != -1 && getContent(blobs, change.getNewBlobInfo(), request.getTarget().getRepository()).isEmpty())
				end = -1;
		}

		int begin = -1;
		if (change.getOldPath() != null) { 
			begin = commits.indexOf(change.getOldRevision());
			if (begin != -1 && getContent(blobs, change.getOldBlobInfo(), request.getTarget().getRepository()).isEmpty())
				begin = -1;
		}

		if (end != -1) {
			for (int i=end-1; i>begin; i--) {
				String commit = commits.get(i);
				Map<String, List<InlineComment>> commentsOnCommit = getCommentsOnCommit(comments, commit, request);
				BlobInfo blobInfo = null;
				List<InlineComment> commentsOnFile = commentsOnCommit.get(change.getNewPath());
				if (commentsOnFile != null) {
					blobInfo = new BlobInfo(commit, change.getNewPath(), change.getNewMode());
				} else { 
					commentsOnFile = commentsOnCommit.get(change.getOldPath());
					if (commentsOnFile != null)
						blobInfo = new BlobInfo(commit, change.getOldPath(), change.getOldMode());
				}
				
				if (blobInfo != null) {
					List<String> fileContent = getContent(blobs, blobInfo, request.getTarget().getRepository());
					if (!fileContent.isEmpty()) {
						List<String> newContent = getContent(blobs, change.getNewBlobInfo(), request.getTarget().getRepository());
						Map<Integer, Integer> lineMap = DiffUtils.mapLines(fileContent, newContent);
						for (InlineComment comment: commentsOnFile) {
							Integer line = lineMap.get(comment.getLine());
							if (line != null)
								addLineComment(newComments, line, comment);
						}
					}
				}
			}

			List<InlineComment> commentsOnFile = getCommentsOnCommit(comments, change.getNewRevision(), request).get(change.getNewPath());
			if (commentsOnFile != null) {
				for (InlineComment comment: commentsOnFile)
					addLineComment(newComments, comment.getLine(), comment);
			}
		}

		if (begin != -1) {
			Set<CommentKey> appliedComments = new HashSet<>();
			for (List<InlineComment> list: newComments.values()) {
				for (InlineComment comment: list)
					appliedComments.add(new CommentKey(comment.getCommitHash(), comment.getFile(), comment.getLine()));
			}

			for (int i=begin+1; i<(end!=-1?end:commits.size()); i++) {
				String commit = commits.get(i);
				Map<String, List<InlineComment>> commentsOnCommit = getCommentsOnCommit(comments, commit, request);
				BlobInfo blobInfo = null;
				List<InlineComment> commentsOnFile = commentsOnCommit.get(change.getOldPath());
				if (commentsOnFile != null) {
					blobInfo = new BlobInfo(commit, change.getOldPath(), change.getOldMode());
				} else { 
					commentsOnFile = commentsOnCommit.get(change.getNewPath());
					if (commentsOnFile != null)
						blobInfo = new BlobInfo(commit, change.getNewPath(), change.getNewMode());
				}
				
				if (blobInfo != null) {
					boolean allApplied = true;
					for (InlineComment comment: commentsOnFile) {
						CommentKey key = new CommentKey(comment.getCommitHash(), comment.getFile(), comment.getLine());
						if (!appliedComments.contains(key)) {
							allApplied = false;
							break;
						}
					}
					if (!allApplied) {
						List<String> fileContent = getContent(blobs, blobInfo, request.getTarget().getRepository());
						if (!fileContent.isEmpty()) {
							List<String> oldContent = getContent(blobs, change.getOldBlobInfo(), request.getTarget().getRepository());
							Map<Integer, Integer> lineMap = DiffUtils.mapLines(fileContent, oldContent);
							for (InlineComment comment: commentsOnFile) {
								CommentKey key = new CommentKey(comment.getCommitHash(), comment.getFile(), comment.getLine());
								Integer line = lineMap.get(comment.getLine());
								if (!appliedComments.contains(key) && line != null)
									addLineComment(oldComments, line, comment);
							}
						}
					}
				}
			}

			if (!change.getOldRevision().equals(change.getNewRevision())) {
				List<InlineComment> commentsOnFile = getCommentsOnCommit(comments, change.getOldRevision(), request).get(change.getOldPath());
				if (commentsOnFile != null) {
					for (InlineComment comment: commentsOnFile)
						addLineComment(oldComments, comment.getLine(), comment);
				}
			}
		}
		
	}
	
	private void addLineComment(Map<Integer, List<InlineComment>> comments, int line, InlineComment comment) {
		List<InlineComment> commentsOnLine = comments.get(line);
		if (commentsOnLine == null) {
			commentsOnLine = new ArrayList<>();
			comments.put(line, commentsOnLine);
		}
		commentsOnLine.add(comment);
	}
	
	private Map<String, List<InlineComment>> getCommentsOnCommit(Map<String, Map<String, List<InlineComment>>> comments, 
			String commit, PullRequest request) {
		Map<String, List<InlineComment>> commentsOnCommit = comments.get(commit);
		if (commentsOnCommit == null) {
			commentsOnCommit = new HashMap<>();

			for (PullRequestComment each: request.getComments()) {
				if (each.getInlineInfo() != null && each.getInlineInfo().getCommitHash().equals(commit)) {
					List<InlineComment> commentsOnFile = commentsOnCommit.get(each.getFile());
					if (commentsOnFile == null) {
						commentsOnFile = new ArrayList<>();
						commentsOnCommit.put(each.getFile(), commentsOnFile);
					}
					commentsOnFile.add(each);
				}
			}
			comments.put(commit, commentsOnCommit);
		}
		return commentsOnCommit;
	}

	public Map<Integer, List<InlineComment>> getOldComments() {
		return oldComments;
	}

	public Map<Integer, List<InlineComment>> getNewComments() {
		return newComments;
	}
	
	private List<String> getContent(Map<BlobInfo, List<String>> blobs, BlobInfo blobInfo, Repository repo) {
		List<String> content = blobs.get(blobInfo);
		if (content == null) {
			BlobText blobText = repo.getBlobText(blobInfo);
			if (blobText != null)
				content = blobText.getLines();
			else
				content = new ArrayList<>();
			blobs.put(blobInfo, content);
		}
		return content;
	}
	
	private static class CommentKey extends Triple<String, String, Integer> {
		
		public CommentKey(String commit, String file, int line) {
			super(commit, file, line);
		}
		
	}
}
