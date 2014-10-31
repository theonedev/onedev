package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
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
	
		if (change.getNewPath() != null) {
			int end = commits.indexOf(change.getNewRevision());
			Preconditions.checkState(end != -1);
			if (!getContent(blobs, change.getNewBlobInfo(), request.getTarget().getRepository()).isEmpty()) {
				for (int i=commits.size()-1; i>end; i--) {
					String commit = commits.get(i);
					Map<String, List<InlineComment>> commentsOnCommit = getCommentsOnCommit(comments, commit, request);
					List<InlineComment> commentsOnFile = commentsOnCommit.get(change.getNewPath());
					if (commentsOnFile != null) {
						Preconditions.checkState(!commentsOnFile.isEmpty());
						BlobInfo blobInfo = commentsOnFile.get(0).getBlobInfo();
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
		}

		if (change.getOldPath() != null) { 
			int begin = commits.indexOf(change.getOldRevision());
			Preconditions.checkState(begin != -1);
			if (!getContent(blobs, change.getOldBlobInfo(), request.getTarget().getRepository()).isEmpty()
					&& !change.getOldRevision().equals(change.getNewRevision())) {
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

			for (PullRequestComment comment: request.getComments()) {
				if (comment.getInlineInfo() != null) {
					GitPlex.getInstance(PullRequestCommentManager.class).updateInline(comment);
					if (comment.getBlobInfo().getRevision().equals(commit)) {
						List<InlineComment> commentsOnFile = commentsOnCommit.get(comment.getBlobInfo().getPath());
						if (commentsOnFile == null) {
							commentsOnFile = new ArrayList<>();
							commentsOnCommit.put(comment.getBlobInfo().getPath(), commentsOnFile);
						}
						commentsOnFile.add(comment);
					}
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
	
}
