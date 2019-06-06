package io.onedev.server.search.entity.build;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;

public class ParamCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Build> root, CriteriaBuilder builder, User user) {
		From<?, ?> join = root.join(BuildConstants.ATTR_PARAMS, JoinType.LEFT);
		return builder.and(
				builder.equal(join.get(BuildParam.ATTR_NAME), name),
				builder.equal(join.get(BuildParam.ATTR_VALUE), value));
	}

	@Override
	public boolean matches(Build build, User user) {
		List<String> paramValues = build.getParamMap().get(name);
		if (paramValues == null || paramValues.isEmpty())
			return value == null;
		else 
			return paramValues.contains(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(name) + " " + BuildQuery.getRuleName(BuildQueryLexer.Is) + " " + BuildQuery.quote(value);
	}
	
}
