package io.onedev.server.model;

import java.io.Serial;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.server.rest.annotation.Immutable;

@Entity
@Table(
		indexes={@Index(columnList="user_id"), @Index(columnList="request_id"), @Index(columnList="update_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"user_id", "request_id"})}
)
public class PullRequestReview extends AbstractEntity {

	@Serial
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
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private PullRequestUpdate update;

	/** The update on which this reviewer last approved or requested changes. */
	@Nullable
	public PullRequestUpdate getUpdate() {
		return update;
	}

	public void setUpdate(PullRequestUpdate update) {
		this.update = update;
	}

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
	}

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

}
