package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.onedev.server.rest.annotation.Immutable;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="o_user_id"), @Index(columnList="o_request_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_user_id", "o_request_id"})}
)
public class PullRequestReview extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_REQUEST = "request";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_STATUS = "status";
	
	public enum Status {PENDING, APPROVED, REQUESTED_FOR_CHANGES, EXCLUDED}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private PullRequest request;
	
	@Column(nullable=false)
	private Status status = Status.PENDING;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Date statusDate = new Date();
	
	private transient boolean dirty;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		statusDate = new Date();
		dirty = true;
	}

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}

}
