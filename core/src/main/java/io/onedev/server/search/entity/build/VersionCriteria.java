package io.onedev.server.search.entity.build;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.build.BuildConstants;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;

public class VersionCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public VersionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<String> attribute = context.getRoot().get(BuildConstants.ATTR_VERSION);
		String normalized = value.toLowerCase().replace("*", "%");
		return context.getBuilder().like(context.getBuilder().lower(attribute), normalized);
	}

	@Override
	public boolean matches(Build build, User user) {
		return build.getVersion().toLowerCase().contains(value.toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_VERSION) + " " + BuildQuery.getRuleName(BuildQueryLexer.Matches) + " " + BuildQuery.quote(value);
	}

}
