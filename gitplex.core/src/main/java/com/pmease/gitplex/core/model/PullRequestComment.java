package com.pmease.gitplex.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.diff.AroundContext;
import com.pmease.commons.util.Pair;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.manager.PullRequestCommentReplyManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.UserManager;

@SuppressWarnings("serial")
@Entity
public class PullRequestComment extends AbstractEntity implements InlineComment {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.JOIN)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false, length=65535)
	private String content;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@OneToMany(mappedBy="comment")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<PullRequestCommentReply> replies = new ArrayList<>();
	
	@Embedded
	private InlineInfo inlineInfo;
	
	private transient Pair<String, String> oldCommitAndNewCommit;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	@Nullable
	@Override
	public User getUser() {
		return user;
	}

	public void setUser(@Nullable User user) {
		this.user = user;
	}

	@Override
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public void saveContent(String content) {
		setContent(content);
		GitPlex.getInstance(Dao.class).persist(this);
	}

	@Override
	public Repository getRepository() {
		return request.getTargetRepo();
	}

	@Override
	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
	}

	@Override
	public Collection<PullRequestCommentReply> getReplies() {
		return request.getCommentReplies(this);
	}

	@Override
	public CommentReply addReply(String content) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		Preconditions.checkNotNull(user);
		PullRequestCommentReply reply = new PullRequestCommentReply();
		reply.setUser(user);
		reply.setDate(new Date());
		reply.setContent(content);
		reply.setComment(this);
		GitPlex.getInstance(PullRequestCommentReplyManager.class).save(reply);
		return reply;
	}

	private Pair<String, String> getOldCommitAndNewCommit() {
		if (oldCommitAndNewCommit == null) {
			List<String> commitHashes = getRequest().getCommentables();
			int index = commitHashes.indexOf(getBlobIdent().revision);
			int compareIndex = commitHashes.indexOf(getCompareWith().revision);
			Preconditions.checkState(index != -1 && compareIndex != -1);
			if (index <= compareIndex)
				oldCommitAndNewCommit = new Pair<>(getBlobIdent().revision, getCompareWith().revision);
			else 
				oldCommitAndNewCommit = new Pair<>(getCompareWith().revision, getBlobIdent().revision);
		}
		return oldCommitAndNewCommit;
	}
	
	public InlineInfo getInlineInfo() {
		return inlineInfo;
	}

	public void setInlineInfo(InlineInfo inlineInfo) {
		this.inlineInfo = inlineInfo;
	}

	public String getOldCommitHash() {
		return getOldCommitAndNewCommit().getFirst();
	}
	
	public String getNewCommitHash() {
		return getOldCommitAndNewCommit().getSecond();
	}

	@Override
	public BlobIdent getBlobIdent() {
		return Preconditions.checkNotNull(inlineInfo).getBlobIdent();
	}
	
	public void setBlobIdent(BlobIdent blobIdent) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setBlobIdent(blobIdent);
	}
	
	public BlobIdent getCompareWith() {
		return Preconditions.checkNotNull(inlineInfo).getCompareWith();
	}
	
	public void setCompareWith(BlobIdent compareWith) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setCompareWith(compareWith);
	}

	@Override
	public int getLine() {
		return Preconditions.checkNotNull(inlineInfo).getLine();
	}

	public void setLine(int line) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setLine(line);
	}
	
	@Override
	public AroundContext getContext() {
		return Preconditions.checkNotNull(inlineInfo).getContext();
	}
	
	public void setContext(AroundContext context) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setContext(context);
	}

	@Override
	public Date getLastVisitDate() {
		return GitPlex.getInstance(PullRequestManager.class).getLastVisitDate(getRequest());
	}

	@Override
	public CommentReply loadReply(Long replyId) {
		return GitPlex.getInstance(Dao.class).load(PullRequestCommentReply.class, replyId);
	}

}
