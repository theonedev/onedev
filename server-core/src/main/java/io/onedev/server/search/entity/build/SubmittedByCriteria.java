package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class SubmittedByCriteria extends Criteria<Build> {

	@Nullable
	public abstract User getUser();
	
}
