package io.onedev.server.search.entity.pack;

import io.onedev.server.model.Pack;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

import javax.persistence.criteria.*;

import static io.onedev.server.util.match.WildcardUtils.matchString;

public class GroupIdCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public GroupIdCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Pack.PROP_GROUP_ID);
		String normalized = value.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == PackQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Pack pack) {
		String groupId = pack.getGroupId();
		var matches = groupId != null && matchString(value.toLowerCase(), groupId.toLowerCase());
		if (operator == PackQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Pack.NAME_GROUP_ID) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
