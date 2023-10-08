package io.onedev.server.manager;

import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestAssignmentManager extends EntityManager<PullRequestAssignment> {

    void create(PullRequestAssignment assignment);
}
