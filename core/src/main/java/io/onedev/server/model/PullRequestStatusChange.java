package io.onedev.server.model;

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

@Entity
@Table(indexes={@Index(columnList="g_request_id"), @Index(columnList="g_user_id")})
@DynamicUpdate
public class PullRequestStatusChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public enum Type {
		APPROVED("approved", "fa fa-thumbs-o-up"),
		DISAPPROVED("disapproved", "fa fa-thumbs-o-down"),
		DISCARDED("discarded", "fa fa-ban"),
		MERGED("merged", "fa fa-check"),
		REOPENED("reopened", "fa fa-repeat"),
		WITHDRAWED_REVIEW("withdrawed review", "fa fa-undo"),
		ADDED_REVIEWER("added reviewer", "fa fa-plus"),
		REMOVED_REVIEWER("removed reviewer", "fa fa-trash"),
		CHANGED_MERGE_STRATEGY("changed merge strategy", "fa fa-pencil"),
		DELETED_SOURCE_BRANCH("deleted source branch", "fa fa-trash"),
		RESTORED_SOURCE_BRANCH("restored source branch", "fa fa-repeat"),
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
	@JoinColumn
	private User user;
	
	private String userName;
	
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
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Nullable
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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
