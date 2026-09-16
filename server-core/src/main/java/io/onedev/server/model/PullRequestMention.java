package io.onedev.server.model;

import jakarta.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="request_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"request_id", "user_id"})}
)
public class PullRequestMention extends AbstractEntity {

	public static final String PROP_USER = "user";
	
	public static final String PROP_REQUEST = "request";
	
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
