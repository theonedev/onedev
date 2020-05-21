package io.onedev.server.model;

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
public class PullRequestAssignment extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_USER = "user";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
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
