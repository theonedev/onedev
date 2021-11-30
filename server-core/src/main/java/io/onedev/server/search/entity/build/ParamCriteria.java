package io.onedev.server.search.entity.build;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.search.entity.EntityCriteria;

public class ParamCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Build> root, CriteriaBuilder builder) {
		Subquery<BuildParam> paramQuery = query.subquery(BuildParam.class);
		Root<BuildParam> param = paramQuery.from(BuildParam.class);
		paramQuery.select(param);

		return builder.exists(paramQuery.where(
				builder.equal(param.get(BuildParam.PROP_BUILD), root), 
				builder.equal(param.get(BuildParam.PROP_NAME), name), 
				builder.equal(param.get(BuildParam.PROP_VALUE), value)));
	}

	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		return paramValues != null && paramValues.contains(value);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ quote(value);
	}
	
}
