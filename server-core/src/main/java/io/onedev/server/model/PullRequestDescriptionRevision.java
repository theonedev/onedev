package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import io.onedev.server.model.support.CommentRevision;

@Entity
public class PullRequestDescriptionRevision extends CommentRevision {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;

    public PullRequest getRequest() {
        return request;
    }

    public void setRequest(PullRequest request) {
        this.request = request;
    }

}
