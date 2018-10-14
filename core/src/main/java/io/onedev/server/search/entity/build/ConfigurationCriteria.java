package io.onedev.server.search.entity.build;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.BuildConstants;

public class ConfigurationCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private Configuration value;
	
	public ConfigurationCriteria(Configuration value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<?> attribute = context.getRoot().get(BuildConstants.ATTR_CONFIGURATION);
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(Build build, User user) {
		return build.getConfiguration().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_CONFIGURATION) + " " + BuildQuery.getRuleName(BuildQueryLexer.Is) + " " + BuildQuery.quote(value.getName());
	}

}
