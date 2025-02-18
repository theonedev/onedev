package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
        indexes={@Index(columnList="o_comment_id"), @Index(columnList="o_user_id")},
        uniqueConstraints={@UniqueConstraint(columnNames={"o_comment_id", "o_user_id", "emoji"})}
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