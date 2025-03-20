package io.onedev.server.search.entity.pack;

import io.onedev.server.model.Pack;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class PublishedByCriteria extends Criteria<Pack> {

	public abstract User getUser();
	
}
