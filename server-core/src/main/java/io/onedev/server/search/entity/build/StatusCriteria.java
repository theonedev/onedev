package io.onedev.server.search.entity.build;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public abstract class StatusCriteria extends Criteria<Build> {

	public abstract Build.Status getStatus();
}
