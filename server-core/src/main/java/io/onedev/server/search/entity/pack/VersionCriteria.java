package io.onedev.server.search.entity.pack;

import static io.onedev.commons.utils.match.WildcardUtils.matchString;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Pack;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class VersionCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public VersionCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
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
		var matches = version != null && matchString(value.toLowerCase(), version.toLowerCase());
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
