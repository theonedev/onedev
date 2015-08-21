package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Change;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.comment.MentionParser;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultPullRequestCommentManager implements PullRequestCommentManager {

	private final Dao dao;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, MarkdownManager markdownManager, 
			Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.markdownManager = markdownManager;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void updateInline(PullRequestComment comment) {
		Preconditions.checkNotNull(comment.getInlineInfo());
		
		String latestCommitHash = comment.getRequest().getLatestUpdate().getHeadCommitHash();
		if (!comment.getNewCommitHash().equals(latestCommitHash)) {
			List<Change> changes = comment.getRepository().getChanges(comment.getNewCommitHash(), latestCommitHash);
			String oldCommitHash = comment.getOldCommitHash();
			if (oldCommitHash.equals(comment.getBlobIdent().revision)) {
				BlobIdent newBlobIdent = null;
				if (comment.getCompareWith().path != null) {
					for (Change change: changes) {
						if (comment.getCompareWith().path.equals(change.getOldBlobIdent().path)) {
							newBlobIdent = new BlobIdent(latestCommitHash, change.getNewBlobIdent().path, change.getNewBlobIdent().mode);
							break;
						}
					}
				} else {
					for (Change change: changes) {
						if (comment.getBlobIdent().path.equals(change.getNewBlobIdent().path)) {
							newBlobIdent = new BlobIdent(latestCommitHash, change.getNewBlobIdent().path, change.getNewBlobIdent().mode);
							break;
						}
					}
				}
				if (newBlobIdent != null) {
					Blob.Text oldText = comment.getRepository().getBlob(comment.getBlobIdent()).getText();
					Preconditions.checkNotNull(oldText);
					comment.setCompareWith(newBlobIdent);
				} else {
					comment.getCompareWith().revision = latestCommitHash;
				}
			} else {
				BlobIdent newBlobIdent = null;
				for (Change change: changes) {
					if (comment.getBlobIdent().path.equals(change.getOldBlobIdent().path)) {
						newBlobIdent = new BlobIdent(latestCommitHash, change.getNewBlobIdent().path, change.getNewBlobIdent().mode);
						break;
					}
				}
				if (newBlobIdent != null) {
					Blob.Text oldText = comment.getRepository().getBlob(comment.getBlobIdent()).getText();
					Preconditions.checkNotNull(oldText);
					List<String> newLines;
					if (newBlobIdent.path != null) {
						Blob.Text newText = comment.getRepository().getBlob(newBlobIdent).getText();
						if (newText != null)
							newLines = newText.getLines();
						else
							newLines = null;
					} else {
						newLines = new ArrayList<>();
					}
					if (newLines != null) {
						List<DiffBlock<String>> diffs = DiffUtils.diff(oldText.getLines(), newLines);
						Integer newLineNo = DiffUtils.mapLines(diffs).get(comment.getLine());
						if (newLineNo != null) {
							comment.setBlobIdent(newBlobIdent);
							comment.setLine(newLineNo);
						} else {
							comment.setCompareWith(newBlobIdent);
						}
					} else {
						comment.setCompareWith(newBlobIdent);
					}
				} else {
					comment.getBlobIdent().revision = latestCommitHash;
				}
			}
			dao.persist(comment);
		}
	}
	
	@Transactional
	@Override
	public void save(PullRequestComment comment, boolean notify) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		
		if (isNew) {
			String rawHtml = markdownManager.parse(comment.getContent());
			Collection<User> mentions = new MentionParser().parseMentions(rawHtml);
			for (User user: mentions) {
				for (PullRequestListener listener: pullRequestListeners)
					listener.onMentioned(comment, user);
			}
		}
		
		if (notify) {
			for (PullRequestListener listener: pullRequestListeners)
				listener.onCommented(comment);
		}
	}

}
