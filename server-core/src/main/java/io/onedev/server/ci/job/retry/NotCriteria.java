package io.onedev.server.ci.job.retry;

import io.onedev.server.model.Build;

public class NotCriteria extends Criteria {
	
	private static final long serialVersionUID = 1L;

	private final Criteria criteria;
	
	public NotCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public boolean satisfied(Build build) {
		return !criteria.satisfied(build);
	}

}
