package io.onedev.server.search.entity.build;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.BuildConstants;

public class CancelledByMeCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<User> attribute = context.getRoot().get(BuildConstants.ATTR_CANCELLER);
		return context.getBuilder().equal(attribute, user);
	}

	@Override
	public boolean matches(Build build, User user) {
		return Objects.equals(build.getCanceller(), user);
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.CancelledByMe);
	}

}
