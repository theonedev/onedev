package io.onedev.server.ci.job.retry;

import java.util.List;

import io.onedev.server.model.Build;

public class AndCriteria extends Criteria {
	
	private static final long serialVersionUID = 1L;

	private final List<? extends Criteria> criterias;
	
	public AndCriteria(List<? extends Criteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public boolean satisfied(Build build) {
		for (Criteria criteria: criterias) {
			if (!criteria.satisfied(build))
				return false;
		}
		return true;
	}

}
