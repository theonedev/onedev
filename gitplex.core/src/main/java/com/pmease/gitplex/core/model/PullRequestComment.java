package com.pmease.gitplex.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.comment.InlineContext;
import com.pmease.gitplex.core.manager.UserManager;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"commitHash", "file", "line"})
})
public class PullRequestComment extends AbstractEntity implements InlineComment {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false)
	private String content;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	private InlineInfo inlineInfo;
	
	private boolean resolved;

	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<PullRequestCommentReply> replies = new ArrayList<>();

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	@Nullable
	public User getUser() {
		return user;
	}

	public void setUser(@Nullable User user) {
		this.user = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public InlineInfo getInlineInfo() {
		return inlineInfo;
	}

	public void setInlineInfo(InlineInfo inlineInfo) {
		this.inlineInfo = inlineInfo;
	}

	@Override
	public Collection<PullRequestCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<PullRequestCommentReply> replies) {
		this.replies = replies;
	}

	@Override
	public void saveContent(String content) {
		setContent(content);
		GitPlex.getInstance(Dao.class).persist(this);
	}

	@Override
	public Repository getRepository() {
		return request.getTarget().getRepository();
	}

	@Override
	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
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
		GitPlex.getInstance(Dao.class).persist(reply);
		return reply;
	}

	@Override
	public int getLine() {
		return Preconditions.checkNotNull(inlineInfo).getLine();
	}

	@Override
	public String getCommitHash() {
		return Preconditions.checkNotNull(inlineInfo).getCommitHash();
	}

	@Override
	public String getOldCommitHash() {
		return Preconditions.checkNotNull(inlineInfo).getOldCommitHash();
	}

	@Override
	public String getNewCommitHash() {
		return Preconditions.checkNotNull(inlineInfo).getNewCommitHash();
	}

	@Override
	public String getFile() {
		return Preconditions.checkNotNull(inlineInfo).getFile();
	}

	@Override
	public InlineContext getContext() {
		return Preconditions.checkNotNull(inlineInfo).getContext();
	}

	@Override
	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	@Override
	public void resolve(boolean resolved) {
		setResolved(resolved);
		GitPlex.getInstance(Dao.class).persist(this);
	}

}
