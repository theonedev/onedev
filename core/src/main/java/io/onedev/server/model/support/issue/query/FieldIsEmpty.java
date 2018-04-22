package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

public class FieldIsEmpty extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	public FieldIsEmpty(String name) {
		super(name);
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return context.getBuilder().isNull(context.getJoin(getFieldName()).get("value"));
	}

}
