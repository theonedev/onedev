package io.onedev.server.model;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

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

import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.server.model.support.EntityComment;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.util.facade.PullRequestCommentFacade;

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

	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<PullRequestCommentRevision> revisions = new ArrayList<>();

	@JsonProperty(access = READ_ONLY)
	private int revisionCount;

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

	public Collection<PullRequestCommentRevision> getRevisions() {
		return revisions;
	}

	public void setRevisions(Collection<PullRequestCommentRevision> revisions) {
		this.revisions = revisions;
	}

	public PullRequestCommentFacade getFacade() {
		return new PullRequestCommentFacade(getId(), getRequest().getId(), getContent());
	}

	public int getRevisionCount() {
		return revisionCount;
	}

	public void setRevisionCount(int revisionCount) {
		this.revisionCount = revisionCount;
	}
	
}
