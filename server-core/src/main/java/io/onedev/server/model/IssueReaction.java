package io.onedev.server.model;

import jakarta.persistence.*;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
        indexes={@Index(columnList="issue_id"), @Index(columnList="user_id")},
        uniqueConstraints={@UniqueConstraint(columnNames={"issue_id", "user_id", "emoji"})}
)
public class IssueReaction extends EntityReaction {

    private static final long serialVersionUID = 1L;

    public static final String PROP_ISSUE = "issue";

    @ManyToOne
    @JoinColumn(nullable=false)
    private Issue issue;

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    @Override
    protected AbstractEntity getEntity() {
        return issue;
    }

} 