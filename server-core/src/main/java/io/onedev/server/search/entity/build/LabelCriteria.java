package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildLabel;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;

public class LabelCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final LabelSpec labelSpec;
	
	public LabelCriteria(LabelSpec labelSpec) {
		this.labelSpec = labelSpec;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		var labelQuery = query.subquery(BuildLabel.class);
		var labelRoot = labelQuery.from(BuildLabel.class);
		labelQuery.select(labelRoot);

		return builder.exists(labelQuery.where(
				builder.equal(labelRoot.get(BuildLabel.PROP_BUILD), from), 
				builder.equal(labelRoot.get(BuildLabel.PROP_SPEC), labelSpec)));
	}

	@Override
	public boolean matches(Build build) {
		return build.getLabels().stream().anyMatch(it->it.getSpec().equals(labelSpec));
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_LABEL) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ Criteria.quote(labelSpec.getName());
	}

}
