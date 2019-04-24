package io.onedev.server.search.entity.build;

import java.util.Objects;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;

public class ParamCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		From<?, ?> join = context.getJoin(name);
		return context.getBuilder().equal(join.get(BuildParam.FIELD_ATTR_VALUE), value);
	}

	@Override
	public boolean matches(Build build, User user) {
		Object fieldValue = build.getParamMap().get(name);
		return Objects.equals(fieldValue, value);
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
