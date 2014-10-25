package com.pmease.gitplex.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.diff.AroundContext;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.manager.UserManager;

@Entity
@SuppressWarnings("serial")
public class PullRequestInlineComment extends AbstractPullRequestComment implements InlineComment {
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<PullRequestInlineCommentReply> replies = new ArrayList<>();
	
	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name="revision", column=@Column(name="G_BLOB_REV", nullable=false)),
        @AttributeOverride(name="path", column=@Column(name="G_BLOB_PATH", nullable=false)),
        @AttributeOverride(name="mode", column=@Column(name="G_BLOB_MODE", nullable=false))
    })
 	private BlobInfo blobInfo;

	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name="revision", column=@Column(name="G_COMPARE_REV", nullable=false) ),
        @AttributeOverride(name="path", column=@Column(name="G_COMPARE_PATH")),
        @AttributeOverride(name="mode", column=@Column(name="G_COMPARE_MODE", nullable=false))
    })
	private BlobInfo compareWith;
	
	@Column(nullable=false)
	private int line;

	@Lob
	private AroundContext context;
	
	private transient Pair<String, String> oldCommitAndNewCommit;

	@Override
	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	/**
	 * Inline context to be displayed in pull request activities page.
	 * 
	 * @return
	 * 			null if inline context can not be extracted due to one side 
	 * 			of the diff is a binary file
	 */
	@Nullable
	@Override
	public AroundContext getContext() {
		return context;
	}

	public void setContext(@Nullable AroundContext context) {
		this.context = context;
	}

	public BlobInfo getCompareWith() {
		return compareWith;
	}

	public void setCompareWith(BlobInfo compareWith) {
		this.compareWith = compareWith;
	}

	private Pair<String, String> getOldCommitAndNewCommit() {
		if (oldCommitAndNewCommit == null) {
			List<String> commitHashes = getRequest().getCommentables();
			int index = commitHashes.indexOf(getBlobInfo().getRevision());
			int compareIndex = commitHashes.indexOf(getCompareWith().getRevision());
			Preconditions.checkState(index != -1 && compareIndex != -1);
			if (index <= compareIndex)
				oldCommitAndNewCommit = new Pair<>(getBlobInfo().getRevision(), getCompareWith().getRevision());
			else 
				oldCommitAndNewCommit = new Pair<>(getCompareWith().getRevision(), getBlobInfo().getRevision());
		}
		return oldCommitAndNewCommit;
	}
	
	public String getOldCommitHash() {
		return getOldCommitAndNewCommit().getFirst();
	}
	
	public String getNewCommitHash() {
		return getOldCommitAndNewCommit().getSecond();
	}

	@Override
	public BlobInfo getBlobInfo() {
		return blobInfo;
	}

	public void setBlobInfo(BlobInfo blobInfo) {
		this.blobInfo = blobInfo;
	}

	@Override
	public Collection<PullRequestInlineCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<PullRequestInlineCommentReply> replies) {
		this.replies = replies;
	}

	@Override
	public CommentReply addReply(String content) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		Preconditions.checkNotNull(user);
		PullRequestInlineCommentReply reply = new PullRequestInlineCommentReply();
		reply.setUser(user);
		reply.setDate(new Date());
		reply.setContent(content);
		reply.setComment(this);
		GitPlex.getInstance(Dao.class).persist(reply);
		return reply;
	}

}
