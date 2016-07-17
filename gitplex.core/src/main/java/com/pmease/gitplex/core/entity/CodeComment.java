package com.pmease.gitplex.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.component.CodeCommentEvent;
import com.pmease.gitplex.core.entity.component.CommentPos;
import com.pmease.gitplex.core.entity.component.CompareContext;
import com.pmease.gitplex.core.manager.VisitInfoManager;
import com.pmease.gitplex.core.security.SecurityUtils;

/*
 * @DynamicUpdate annotation here along with various @OptimisticLock annotations
 * on certain fields tell Hibernate not to perform version check on those fields
 * which can be updated from background thread.
 */
@Entity
@Table(indexes={@Index(columnList="uuid"), @Index(columnList="commit"), 
		@Index(columnList="path"), @Index(columnList="compareCommit")})
@DynamicUpdate 
public class CodeComment extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	@Version
	private long version;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Depot depot;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Account user;

	@Column(nullable=false)
	private String title;
	
	@Lob
	@Column(nullable=false, length=65535)
	private String content;
	
	@Column(nullable=false)
	private Date createDate = new Date();

	@Column(nullable=false)
	private CodeCommentEvent lastEvent;
	
	@Column(nullable=false)
	private Date lastEventDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Account lastEventUser;

	@Embedded
	private CommentPos commentPos;
	
	@Embedded
	private CompareContext compareContext;
	
	private boolean resolved;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentReply> replies = new ArrayList<>();
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentRelation> requestRelations = new ArrayList<>();
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private transient List<CodeCommentReply> sortedReplies;
	
	public Depot getDepot() {
		return depot;
	}
	
	public void setDepot(Depot depot) {
		this.depot = depot;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Nullable
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public CodeCommentEvent getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(CodeCommentEvent lastEvent) {
		this.lastEvent = lastEvent;
	}

	public Date getLastEventDate() {
		return lastEventDate;
	}

	public void setLastEventDate(Date lastEventDate) {
		this.lastEventDate = lastEventDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public CommentPos getCommentPos() {
		return commentPos;
	}

	public void setCommentPos(CommentPos commentPos) {
		this.commentPos = commentPos;
	}

	public CompareContext getCompareContext() {
		return compareContext;
	}

	public void setCompareContext(CompareContext compareContext) {
		this.compareContext = compareContext;
	}

	public Collection<CodeCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<CodeCommentReply> replies) {
		this.replies = replies;
	}

	public Collection<CodeCommentRelation> getRequestRelations() {
		return requestRelations;
	}

	public void setRequestRelations(Collection<CodeCommentRelation> requestRelations) {
		this.requestRelations = requestRelations;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public Account getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(Account lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public boolean isVisitedAfter(Date date) {
		Account user = SecurityUtils.getAccount();
		if (user != null) {
			Date visitDate = GitPlex.getInstance(VisitInfoManager.class).getVisitDate(user, this);
			return visitDate != null && visitDate.after(date);
		} else {
			return true;
		}
	}

	public List<CodeCommentReply> getSortedReplies() {
		if (sortedReplies == null) {
			sortedReplies = new ArrayList<>(getReplies());
			sortedReplies.sort(Comparator.comparing(CodeCommentReply::getId));
		}
		return sortedReplies;
	}
	
	public ComparingInfo getComparingInfo() {
		return new ComparingInfo(commentPos.getCommit(), compareContext);
	}
	
	public static class ComparingInfo implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String commit;
		
		private final CompareContext compareContext;
		
		public ComparingInfo(String commit, CompareContext compareContext) {
			this.commit = commit;
			this.compareContext = compareContext;
		}
		
		public String getCommit() {
			return commit;
		}

		public CompareContext getCompareContext() {
			return compareContext;
		}
		
	}
	
}
