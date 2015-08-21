package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.diff.DiffEntry;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.comment.MentionParser;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.model.PullRequestComment;
import static com.pmease.gitplex.core.model.PullRequestComment.DIFF_CONTEXT_SIZE;
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
	public void updateInlineInfo(PullRequestComment comment) {
		Preconditions.checkNotNull(comment.getInlineInfo());
		
		String latestCommitHash = comment.getRequest().getLatestUpdate().getHeadCommitHash();
		if (!comment.getNewCommitHash().equals(latestCommitHash)) {
			List<DiffEntry> changes = comment.getRepository().getDiffs(comment.getNewCommitHash(), 
					latestCommitHash, true);
			String oldCommitHash = comment.getOldCommitHash();
			if (oldCommitHash.equals(comment.getBlobIdent().revision)) {
				BlobIdent newCompareWith = null;
				if (comment.getCompareWith().path != null) {
					for (DiffEntry change: changes) {
						if (comment.getCompareWith().path.equals(change.getOldPath())) {
							newCompareWith = GitUtils.getNewBlobIdent(change, latestCommitHash);
							break;
						}
					}
				} else {
					for (DiffEntry diff: changes) {
						if (comment.getBlobIdent().path.equals(diff.getNewPath())) {
							newCompareWith = GitUtils.getNewBlobIdent(diff, latestCommitHash);
							break;
						}
					}
				}
				if (newCompareWith == null)
					comment.setCompareWith(newCompareWith);
				else 
					comment.getCompareWith().revision = latestCommitHash;
				
				if (comment.getCompareWith().path != null) {
					Blob.Text oldText = comment.getRepository().getBlob(comment.getBlobIdent()).getText();
					Blob.Text newText = comment.getRepository().getBlob(comment.getCompareWith()).getText();
					if (oldText != null && newText != null) {
						List<DiffBlock<String>> diffs = DiffUtils.diff(oldText.getLines(), newText.getLines());
						for (int i=0; i<diffs.size(); i++) {
							DiffBlock<String> diff = diffs.get(i);
							int startOffset = comment.getLine() - diff.getOldStart();
							int endOffset = diff.getOldEnd() - comment.getLine();
							if (startOffset >= 0 && endOffset > 0) {
								if (diff.getOperation() == Operation.EQUAL) {
									int newLine = startOffset + diff.getNewStart();
									if (diffs.size() == 1) {
										comment.setBlobIdent(comment.getCompareWith());
										comment.setLine(newLine);
									} else if (i == 0) {
										if (endOffset > DIFF_CONTEXT_SIZE) {
											comment.setBlobIdent(comment.getCompareWith());
											comment.setLine(newLine);
										}
									} else if (i == diffs.size()-1) {
										if (startOffset >= DIFF_CONTEXT_SIZE) {
											comment.setBlobIdent(comment.getCompareWith());
											comment.setLine(newLine);
										}
									} else if (endOffset > DIFF_CONTEXT_SIZE && startOffset >= DIFF_CONTEXT_SIZE){
										comment.setBlobIdent(comment.getCompareWith());
										comment.setLine(newLine);
									}
								}
								break;
							}
						}
					}
				}
			} else {
				BlobIdent newBlobIdent = null;
				for (DiffEntry diff: changes) {
					if (comment.getBlobIdent().path.equals(diff.getOldPath())) {
						newBlobIdent = GitUtils.getNewBlobIdent(diff, latestCommitHash);
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
