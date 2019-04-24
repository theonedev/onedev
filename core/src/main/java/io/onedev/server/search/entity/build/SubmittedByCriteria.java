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

public class SubmittedByCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	private String rawValue;
	
	public SubmittedByCriteria(User value, String rawValue) {
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<User> attribute = context.getRoot().get(BuildConstants.ATTR_SUBMITTER);
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(Build build, User user) {
		return Objects.equals(build.getSubmitter(), value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}
	
	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.SubmittedBy) + " " + BuildQuery.quote(rawValue);
	}

}
