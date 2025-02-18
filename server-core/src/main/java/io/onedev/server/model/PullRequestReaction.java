package io.onedev.server.model;

import javax.persistence.*;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
        indexes={@Index(columnList="o_request_id"), @Index(columnList="o_user_id")},
        uniqueConstraints={@UniqueConstraint(columnNames={"o_request_id", "o_user_id", "emoji"})}
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