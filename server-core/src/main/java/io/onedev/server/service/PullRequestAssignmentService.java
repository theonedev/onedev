package io.onedev.server.service;

import io.onedev.server.model.PullRequestAssignment;

public interface PullRequestAssignmentService extends EntityService<PullRequestAssignment> {

    void create(PullRequestAssignment assignment);
}
