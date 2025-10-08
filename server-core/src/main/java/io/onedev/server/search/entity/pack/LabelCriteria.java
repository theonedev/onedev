package io.onedev.server.search.entity.pack;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackLabel;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class LabelCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final LabelSpec labelSpec;
	
	private final int operator;
	
	public LabelCriteria(LabelSpec labelSpec, int operator) {
		this.labelSpec = labelSpec;
		this.operator = operator;
	}

	public LabelSpec getLabelSpec() {
		return labelSpec;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		var labelQuery = query.subquery(PackLabel.class);
		var labelRoot = labelQuery.from(PackLabel.class);
		labelQuery.select(labelRoot);

		var predicate = builder.exists(labelQuery.where(
				builder.equal(labelRoot.get(PackLabel.PROP_PACK), from), 
				builder.equal(labelRoot.get(PackLabel.PROP_SPEC), labelSpec)));
		if (operator == PackQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Pack pack) {
		var matches = pack.getLabels().stream().anyMatch(it->it.getSpec().equals(labelSpec));
		if (operator == PackQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Pack.NAME_LABEL) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ Criteria.quote(labelSpec.getName());
	}

}
