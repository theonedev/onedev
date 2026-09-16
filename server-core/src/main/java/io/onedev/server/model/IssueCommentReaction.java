package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
        indexes={@Index(columnList="comment_id"), @Index(columnList="user_id")},
        uniqueConstraints={@UniqueConstraint(columnNames={"comment_id", "user_id", "emoji"})}
)
public class IssueCommentReaction extends EntityReaction {

    private static final long serialVersionUID = 1L;

    public static final String PROP_COMMENT = "comment";

    @ManyToOne
    @JoinColumn(nullable=false)
    private IssueComment comment;

    public IssueComment getComment() {
        return comment;
    }

    public void setComment(IssueComment comment) {
        this.comment = comment;
    }

    @Override
    protected AbstractEntity getEntity() {
        return comment;
    }
    
} 