package io.onedev.server.search.entity.pack;

import io.onedev.server.model.Pack;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;
import java.util.Date;

public class CreateDateCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public CreateDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		Path<Date> attribute = from.get(Pack.PROP_CREATE_DATE);
		if (operator == PackQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Pack pack) {
		if (operator == PackQueryLexer.IsUntil)
			return pack.getCreateDate().before(date);
		else
			return pack.getCreateDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Pack.NAME_CREATE_DATE) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
