package com.pmease.gitplex.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.support.CodeCommentActivity;
import com.pmease.gitplex.core.entity.support.CommentPos;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.entity.support.LastEvent;
import com.pmease.gitplex.core.event.codecomment.CodeCommentEvent;
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
	@JoinColumn(nullable=false)
	private Account user;

	@Lob
	@Column(nullable=false, length=65535)
	private String content;
	
	@Column(nullable=false)
	private Date date = new Date();

	@Embedded
	private LastEvent lastEvent;

	@Embedded
	private CommentPos commentPos;
	
	@Embedded
	private CompareContext compareContext;
	
	private String branchRef;
	
	private boolean resolved;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentReply> replies = new ArrayList<>();
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentRelation> relations = new ArrayList<>();
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentStatusChange> statusChanges= new ArrayList<>();
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private transient List<CodeCommentActivity> activities;
	
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

	public Collection<CodeCommentRelation> getRelations() {
		return relations;
	}

	public void setRelations(Collection<CodeCommentRelation> relations) {
		this.relations = relations;
	}

	public Collection<CodeCommentStatusChange> getStatusChanges() {
		return statusChanges;
	}

	public void setStatusChanges(Collection<CodeCommentStatusChange> statusChanges) {
		this.statusChanges = statusChanges;
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

	@Nullable
	public String getBranchRef() {
		return branchRef;
	}

	public void setBranchRef(String branchRef) {
		this.branchRef = branchRef;
	}

	public LastEvent getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(LastEvent lastEvent) {
		this.lastEvent = lastEvent;
	}
	
	public void setLastEvent(CodeCommentEvent event) {
		LastEvent lastEvent = new LastEvent();
		lastEvent.setDate(event.getDate());
		lastEvent.setType(EditableUtils.getName(event.getClass()));
		lastEvent.setUser(event.getUser());
		setLastEvent(lastEvent);
	}

	public boolean isVisitedAfter(Date date) {
		Account user = SecurityUtils.getAccount();
		if (user != null) {
			Date visitDate = GitPlex.getInstance(VisitInfoManager.class).getVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}

	public ComparingInfo getComparingInfo() {
		return new ComparingInfo(commentPos.getCommit(), compareContext);
	}
	
	public List<CodeCommentActivity> getActivities() {
		if (activities == null) {
			activities= new ArrayList<>();
			activities.addAll(getReplies());
			activities.addAll(getStatusChanges());
			activities.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));
		}
		return activities;
	}
	
	public CompareContext getLastCompareContext() {
		if (!getActivities().isEmpty()) {
			CodeCommentActivity lastActivity = activities.get(activities.size()-1);
			return lastActivity.getCompareContext();
		} else {
			return getCompareContext();
		}
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
