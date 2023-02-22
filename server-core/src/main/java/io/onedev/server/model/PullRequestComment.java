package io.onedev.server.model;

import io.onedev.server.model.support.EntityComment;

import javax.persistence.*;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="o_user_id")})
public class PullRequestComment extends EntityComment {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_REQUEST = "request";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public Project getProject() {
		return request.getTargetProject();
	}

	@Override
	public AbstractEntity getEntity() {
		return getRequest();
	}
	
}
