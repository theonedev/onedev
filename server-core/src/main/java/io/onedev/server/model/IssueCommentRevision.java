package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.onedev.server.model.support.CommentRevision;

@Entity
public class IssueCommentRevision extends CommentRevision {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private IssueComment comment;

    public IssueComment getComment() {
        return comment;
    }

    public void setComment(IssueComment comment) {
        this.comment = comment;
    }
    
}
