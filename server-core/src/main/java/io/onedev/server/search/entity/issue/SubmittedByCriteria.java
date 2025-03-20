package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class SubmittedByCriteria extends Criteria<Issue> {

	public abstract User getUser();
	
}
