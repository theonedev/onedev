package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

public class FieldIsNotEmpty extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	public FieldIsNotEmpty(String name) {
		super(name);
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return context.getBuilder().isNotNull(context.getJoin(getFieldName()).get("value"));
	}

}
