package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Workspace;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class CreatedByCriteria extends Criteria<Workspace> {

	@Nullable
	public abstract User getCreator();

}
