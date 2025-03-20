package io.onedev.server.search.entity.codecomment;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class CreatedByCriteria extends Criteria<CodeComment> {

	public abstract User getUser();
	
}
