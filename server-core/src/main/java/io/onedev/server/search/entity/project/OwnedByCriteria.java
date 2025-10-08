package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class OwnedByCriteria extends Criteria<Project> {

    @Nullable
    public abstract User getUser();

}
