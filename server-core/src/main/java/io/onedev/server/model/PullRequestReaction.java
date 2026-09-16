package io.onedev.server.model;

import jakarta.persistence.*;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
        indexes={@Index(columnList="request_id"), @Index(columnList="user_id")},
        uniqueConstraints={@UniqueConstraint(columnNames={"request_id", "user_id", "emoji"})}
)
public class PullRequestReaction extends EntityReaction {

    private static final long serialVersionUID = 1L;

    public static final String PROP_REQUEST = "request";

    @ManyToOne
    @JoinColumn(nullable=false)
    private PullRequest request;

    public PullRequest getRequest() {
        return request;
    }

    public void setRequest(PullRequest request) {
        this.request = request;
    }

    @Override
    protected AbstractEntity getEntity() {
        return request;
    }

} 