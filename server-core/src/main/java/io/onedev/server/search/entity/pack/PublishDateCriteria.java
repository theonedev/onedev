package io.onedev.server.search.entity.pack;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Pack;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PublishDateCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public PublishDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	public Date getDate() {
		return date;
	}

	public int getOperator() {
		return operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		Path<Date> attribute = from.get(Pack.PROP_PUBLISH_DATE);
		if (operator == PackQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Pack pack) {
		if (operator == PackQueryLexer.IsUntil)
			return pack.getPublishDate().before(date);
		else
			return pack.getPublishDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Pack.NAME_PUBLISH_DATE) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
