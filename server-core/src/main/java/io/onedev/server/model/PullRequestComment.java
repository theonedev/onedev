package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.onedev.server.model.support.EntityComment;
import io.onedev.server.rest.annotation.Immutable;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="o_user_id")})
public class PullRequestComment extends EntityComment {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_REQUEST = "request";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private PullRequest request;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<PullRequestCommentReaction> reactions = new ArrayList<>();

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
	
	public Collection<PullRequestCommentReaction> getReactions() {
		return reactions;
	}

	public void setReactions(Collection<PullRequestCommentReaction> reactions) {
		this.reactions = reactions;
	}
	
}
