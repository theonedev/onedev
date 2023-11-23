package io.onedev.server.search.entity.build;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildLabel;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class LabelCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final LabelSpec labelSpec;
	
	private final int operator;
	
	public LabelCriteria(LabelSpec labelSpec, int operator) {
		this.labelSpec = labelSpec;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		var labelQuery = query.subquery(BuildLabel.class);
		var labelRoot = labelQuery.from(BuildLabel.class);
		labelQuery.select(labelRoot);

		var predicate = builder.exists(labelQuery.where(
				builder.equal(labelRoot.get(BuildLabel.PROP_BUILD), from), 
				builder.equal(labelRoot.get(BuildLabel.PROP_SPEC), labelSpec)));
		if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = build.getLabels().stream().anyMatch(it->it.getSpec().equals(labelSpec));
		if (operator == BuildQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Build.NAME_LABEL) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ Criteria.quote(labelSpec.getName());
	}

}
