package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.model.Comment.DIFF_CONTEXT_SIZE;

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
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultCommentManager implements CommentManager {

	private final Dao dao;
	
	private final UserManager userManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultCommentManager(Dao dao, UserManager userManager, 
			Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.userManager = userManager;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void updateInlineInfo(Comment comment) {
		Preconditions.checkNotNull(comment.getInlineInfo());
		
		String latestCommitHash = comment.getRequest().getLatestUpdate().getHeadCommitHash();
		if (!latestCommitHash.equals(comment.getNewCommitHash())) {
			List<DiffEntry> changes = comment.getDepot().getDiffs(comment.getNewCommitHash(), 
					latestCommitHash, true);
			String oldCommitHash = comment.getOldCommitHash();
			if (oldCommitHash.equals(comment.getBlobIdent().revision)) {
				BlobIdent newBlobIdent = null;
				String comparePath = comment.getCompareWith().path;
				for (DiffEntry change: changes) {
					if (comparePath != null && comparePath.equals(change.getOldPath()) 
							|| comparePath == null && comment.getBlobIdent().path.equals(change.getNewPath())) {
						newBlobIdent = GitUtils.getNewBlobIdent(change, latestCommitHash);
						break;
					}
				}
				if (newBlobIdent != null) {
					comment.setCompareWith(newBlobIdent);
					Blob.Text oldText = comment.getDepot().getBlob(comment.getBlobIdent()).getText();
					Blob.Text newText = null;
					if (comment.getCompareWith().path != null)
						newText = comment.getDepot().getBlob(comment.getCompareWith()).getText();
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
										comment.setBlobIdent(new BlobIdent(comment.getCompareWith()));
										comment.setLine(newLine);
									} else if (i == 0) {
										if (endOffset > DIFF_CONTEXT_SIZE) {
											comment.setBlobIdent(new BlobIdent(comment.getCompareWith()));
											comment.setLine(newLine);
										}
									} else if (i == diffs.size()-1) {
										if (startOffset >= DIFF_CONTEXT_SIZE) {
											comment.setBlobIdent(new BlobIdent(comment.getCompareWith()));
											comment.setLine(newLine);
										}
									} else if (endOffset > DIFF_CONTEXT_SIZE && startOffset >= DIFF_CONTEXT_SIZE){
										comment.setBlobIdent(new BlobIdent(comment.getCompareWith()));
										comment.setLine(newLine);
									}
								}
								break;
							}
						}
					}
				} else if (comment.getCompareWith().equals(comment.getBlobIdent())){
					BlobIdent blobIdent = comment.getBlobIdent();
					blobIdent.revision = latestCommitHash;
					// call setBlobIdent in order to clear some cached info in comment object
					comment.setBlobIdent(blobIdent);
					
					BlobIdent compareWith = comment.getCompareWith();
					compareWith.revision = latestCommitHash;
					// call setCompareWith in order to clear some cached info in comment object
					comment.setCompareWith(compareWith);
				} else {
					BlobIdent compareWith = comment.getCompareWith();
					compareWith.revision = latestCommitHash;
					// call setCompareWith in order to clear some cached info in comment object
					comment.setCompareWith(compareWith);
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
					Blob.Text oldText = comment.getDepot().getBlob(comment.getBlobIdent()).getText();
					Blob.Text newText = null;
					if (newBlobIdent.path != null)
						newText = comment.getDepot().getBlob(newBlobIdent).getText();
					if (oldText != null && newText != null) {
						List<DiffBlock<String>> diffs = DiffUtils.diff(oldText.getLines(), newText.getLines());
						Integer newLineNo = DiffUtils.mapLines(diffs).get(comment.getLine());
						if (newLineNo != null) {
							comment.setBlobIdent(newBlobIdent);
							comment.setLine(newLineNo);
							
							oldText = null;
							if (comment.getCompareWith().path != null)
								oldText = comment.getDepot().getBlob(comment.getCompareWith()).getText();
							if (oldText != null) {
								diffs = DiffUtils.diff(oldText.getLines(), newText.getLines());
								for (int i=0; i<diffs.size(); i++) {
									DiffBlock<String> diff = diffs.get(i);
									int startOffset = comment.getLine() - diff.getNewStart();
									int endOffset = diff.getNewEnd() - comment.getLine();
									if (startOffset >= 0 && endOffset > 0) {
										if (diff.getOperation() == Operation.EQUAL) {
											if (diffs.size() == 1) {
												comment.setCompareWith(new BlobIdent(comment.getBlobIdent()));
											} else if (i == 0) {
												if (endOffset > DIFF_CONTEXT_SIZE) 
													comment.setCompareWith(new BlobIdent(comment.getBlobIdent()));
											} else if (i == diffs.size()-1) {
												if (startOffset >= DIFF_CONTEXT_SIZE) 
													comment.setCompareWith(new BlobIdent(comment.getBlobIdent()));
											} else if (endOffset > DIFF_CONTEXT_SIZE && startOffset >= DIFF_CONTEXT_SIZE){
												comment.setCompareWith(new BlobIdent(comment.getBlobIdent()));
											}
										}
										break;
									}
								}
							}
						} else {
							comment.setCompareWith(newBlobIdent);
						}
					} else {
						comment.setCompareWith(newBlobIdent);
					}
				} else {
					BlobIdent blobIdent = comment.getBlobIdent();
					blobIdent.revision = latestCommitHash;
					// call setBlobInfo in order to clear some cached info in comment object
					comment.setBlobIdent(blobIdent);
				}
			}
			dao.persist(comment);		
		}
	}
	
	@Transactional
	@Override
	public void save(Comment comment, boolean notify) {
		dao.persist(comment);
		
		if (notify) {
			for (PullRequestListener listener: pullRequestListeners)
				listener.onCommented(comment);
		}
	}

	@Transactional
	@Override
	public Comment addInline(PullRequest request, BlobIdent blobInfo, BlobIdent compareWith, int line, String content) {
		User user = userManager.getCurrent();
		Preconditions.checkNotNull(user);
		Comment comment = new Comment();
		request.getComments().add(comment);
		comment.setUser(user);
		comment.setContent(content);
		comment.setRequest(request);
		comment.setBlobIdent(blobInfo);
		comment.setCompareWith(compareWith);
		comment.setLine(line);
		save(comment, true);
		return comment;
	}

}
