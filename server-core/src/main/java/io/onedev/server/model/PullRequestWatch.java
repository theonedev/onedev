package io.onedev.server.model;

import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.rest.annotation.Immutable;

import jakarta.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="request_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"request_id", "user_id"})
})
public class PullRequestWatch extends EntityWatch {

	private static final long serialVersionUID = 1L;

	public static final String PROP_REQUEST = "request";

	public static final String PROP_USER = "user";

	public static final String PROP_WATCHING = "watching";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User user;
	
	private boolean watching;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	@Override
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public boolean isWatching() {
		return watching;
	}

	@Override
	public void setWatching(boolean watching) {
		this.watching = watching;
	}

	@Override
	public AbstractEntity getEntity() {
		return request;
	}

}
