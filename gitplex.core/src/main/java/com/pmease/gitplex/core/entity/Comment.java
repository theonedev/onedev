package com.pmease.gitplex.core.entity;

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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OptimisticLock;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.Pair;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.component.InlineInfo;
import com.pmease.gitplex.core.manager.CommentReplyManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.AccountManager;

/*
 * @DynamicUpdate annotation here along with various @OptimisticLock annotations
 * on certain fields tell Hibernate not to perform version check on those fields
 * which can be updated from background thread.
 */
@Entity
@DynamicUpdate 
public class Comment extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	public static final int DIFF_CONTEXT_SIZE = 3;

	@Version
	private long version;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Account user;
	
	@Lob
	@Column(nullable=false, length=65535)
	private String content;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@OneToMany(mappedBy="comment")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<CommentReply> replies = new ArrayList<>();
	
	@OptimisticLock(excluded=true)
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
	public Account getUser() {
		return user;
	}

	public void setUser(@Nullable Account user) {
		this.user = user;
	}

	public long getVersion() {
		return version;
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

	public Depot getDepot() {
		return request.getTargetDepot();
	}

	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
	}

	public Collection<CommentReply> getReplies() {
		return request.getCommentReplies(this);
	}

	public CommentReply addReply(String content) {
		Account user = GitPlex.getInstance(AccountManager.class).getCurrent();
		Preconditions.checkNotNull(user);
		CommentReply reply = new CommentReply();
		reply.setUser(user);
		reply.setContent(content);
		reply.setComment(this);
		GitPlex.getInstance(CommentReplyManager.class).save(reply);
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

	public int getBeginLine() {
		return Preconditions.checkNotNull(inlineInfo).getBeginLine();
	}

	public void setBeginLine(int beginLine) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setBeginLine(beginLine);
	}
	
	public int getEndLine() {
		return Preconditions.checkNotNull(inlineInfo).getEndLine();
	}

	public void setEndLine(int endLine) {
		if (inlineInfo == null)
			inlineInfo = new InlineInfo();
		inlineInfo.setEndLine(endLine);
	}
	
	public Date getLastVisitDate() {
		return GitPlex.getInstance(PullRequestManager.class).getLastVisitDate(getRequest());
	}

}
