package io.onedev.server.model.support.issue.query;

import java.util.Date;

import javax.persistence.criteria.Predicate;

public class SubmitDateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final DateOperator operator;
	
	private final Date value;
	
	public SubmitDateCriteria(DateOperator operator, Date value) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return operator.getPredicate(context.getBuilder(), context.getRoot().get("submitDate"), value);
	}

}
