package io.onedev.server.search.entity.workspace;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Workspace;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class CreateDateCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final int operator;

	private final Date date;

	private final String value;

	public CreateDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	public int getOperator() {
		return operator;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<Date> attribute = from.get(Workspace.PROP_CREATE_DATE);
		if (operator == WorkspaceQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Workspace workspace) {
		if (operator == WorkspaceQueryLexer.IsUntil)
			return workspace.getCreateDate().before(date);
		else
			return workspace.getCreateDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Workspace.NAME_CREATE_DATE) + " "
				+ WorkspaceQuery.getRuleName(operator) + " "
				+ quote(value);
	}

}
