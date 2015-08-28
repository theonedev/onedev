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
import com.pmease.commons.util.Pair;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.PullRequestCommentReplyManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.UserManager;

@SuppressWarnings("serial")
@Entity
public class PullRequestComment extends AbstractEntity {
	
	public static final int DIFF_CONTEXT_SIZE = 3;

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
	
	private transient Pair<String, String> oldAndNew;
	
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

	public void saveContent(String content) {
		setContent(content);
		GitPlex.getInstance(Dao.class).persist(this);
	}

	public Repository getRepository() {
		return request.getTargetRepo();
	}

	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
	}

	public Collection<PullRequestCommentReply> getReplies() {
		return request.getCommentReplies(this);
	}

	public PullRequestCommentReply addReply(String content) {
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

	private Pair<String, String> getOldAndNew() {
		String rev = getBlobIdent().revision;
		if (oldAndNew == null) {
			List<String> commitHashes = getRequest().getCommentables();
			String compareRev = getCompareWith().revision;
			int index = commitHashes.indexOf(rev);
			int compareIndex = commitHashes.indexOf(compareRev);
			Preconditions.checkState(index != -1 && compareIndex != -1);
			if (index <= compareIndex)
				oldAndNew = new Pair<>(rev, compareRev);
			else 
				oldAndNew = new Pair<>(compareRev, rev);
		}
		return oldAndNew;
	}
	
	public InlineInfo getInlineInfo() {
		return inlineInfo;
	}

	public void setInlineInfo(InlineInfo inlineInfo) {
		this.inlineInfo = inlineInfo;
		oldAndNew = null;
	}

	public String getOldCommitHash() {
		return getOldAndNew().getFirst();
	}
	
	public String getNewCommitHash() {
		return getOldAndNew().getSecond();
	}

	public BlobIdent getBlobIdent() {
		return Preconditions.checkNotNull(inlineInfo).getBlobIdent();
	}
	
	public void setBlobIdent(BlobIdent blobIdent) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setBlobIdent(blobIdent);
		oldAndNew = null;
	}
	
	public BlobIdent getCompareWith() {
		return Preconditions.checkNotNull(inlineInfo).getCompareWith();
	}
	
	public void setCompareWith(BlobIdent compareWith) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setCompareWith(compareWith);
		oldAndNew = null;
	}

	public int getLine() {
		return Preconditions.checkNotNull(inlineInfo).getLine();
	}

	public void setLine(int line) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setLine(line);
	}
	
	public Date getLastVisitDate() {
		return GitPlex.getInstance(PullRequestManager.class).getLastVisitDate(getRequest());
	}

}
