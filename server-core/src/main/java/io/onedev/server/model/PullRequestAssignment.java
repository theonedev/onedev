package io.onedev.server.model;

import io.onedev.server.rest.annotation.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="user_id"), @Index(columnList="request_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"user_id", "request_id"})}
)
public class PullRequestAssignment extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_REQUEST = "request";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private PullRequest request;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

}
