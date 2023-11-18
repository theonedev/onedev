package io.onedev.server.search.entity.pack;

import io.onedev.server.model.Pack;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

import javax.persistence.criteria.*;

public class VersionCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public VersionCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Pack.PROP_VERSION);
		String normalized = value.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == PackQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Pack pack) {
		String version = pack.getVersion();
		var matches = WildcardUtils.matchString(value.toLowerCase(), version.toLowerCase());
		if (operator == PackQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Pack.NAME_VERSION) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
