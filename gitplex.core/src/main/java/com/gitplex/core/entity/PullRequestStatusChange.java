package com.gitplex.core.entity;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;

import com.gitplex.commons.hibernate.AbstractEntity;

@Entity
@Table(indexes={@Index(columnList="g_request_id"), @Index(columnList="g_user_id")})
@DynamicUpdate
public class PullRequestStatusChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public enum Type {
		APPROVED("approved", "fa fa-thumbs-o-up"),
		DISAPPROVED("disapproved", "fa fa-thumbs-o-down"),
		ASSIGNED("assigned", "fa fa-user"),
		DISCARDED("discarded", "fa fa-ban"),
		INTEGRATED("integrated", "fa fa-check"),
		REOPENED("reopened", "fa fa-repeat"),
		REVIEW_DELETED("deleted review", "fa fa-times"),
		VERIFICATION_DELETED("deleted verification", "fa fa-times"),
		VERIFICATION_FAILED("completed verification (failed)", "fa fa-thumbs-o-down"),
		VERIFICATION_SUCCEEDED("completed verification (successful)", "fa fa-thumbs-o-up"),
		SOURCE_BRANCH_DELETED("deleted source branch", "fa fa-times"),
		SOURCE_BRANCH_RESTORED("restored source branch", "fa fa-repeat"),
		;

		private final String name;
		
		private final String iconClass;
		
		Type(String name, String iconClass) {
			this.name = name;
			this.iconClass = iconClass;
		}
		
		public String getName() {
			return name;
		}
		
		public String getIconClass() {
			return iconClass;
		}
		
	}
	
	@Version
	private long version;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private Type type;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;
	
	@Column(nullable=false)
	private Date date;
	
	private String note;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Nullable
	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getVersion() {
		return version;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
}
