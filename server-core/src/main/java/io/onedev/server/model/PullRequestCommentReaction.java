package io.onedev.server.model;

import jakarta.persistence.*;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
        indexes={@Index(columnList="comment_id"), @Index(columnList="user_id")},
        uniqueConstraints={@UniqueConstraint(columnNames={"comment_id", "user_id", "emoji"})}
)
public class PullRequestCommentReaction extends EntityReaction {

    private static final long serialVersionUID = 1L;

    public static final String PROP_COMMENT = "comment";

    @ManyToOne
    @JoinColumn(nullable=false)
    private PullRequestComment comment;

    public PullRequestComment getComment() {
        return comment;
    }

    public void setComment(PullRequestComment comment) {
        this.comment = comment;
    }

    @Override
    protected AbstractEntity getEntity() {
        return comment;
    }

} 