package io.onedev.server.search.entity.build;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.BuildConstants;

public class TimedOutCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<?> attribute = context.getRoot().get(BuildConstants.ATTR_STATUS);
		return context.getBuilder().equal(attribute, Build.Status.TIMED_OUT);
	}

	@Override
	public boolean matches(Build build, User user) {
		return build.getStatus() == Build.Status.TIMED_OUT;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.TimedOut);
	}

}
