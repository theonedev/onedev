package io.onedev.server.search.entity.build;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.BuildQueryConstants;

public class SubmittedByCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	private final String value;
	
	public SubmittedByCriteria(String value) {
		user = EntityQuery.getUser(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder, User user) {
		Path<User> attribute = root.get(BuildQueryConstants.ATTR_SUBMITTER);
		return builder.equal(attribute, this.user);
	}

	@Override
	public boolean matches(Build build, User user) {
		return Objects.equals(build.getSubmitter(), this.user);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}
	
	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.SubmittedBy) + " " + BuildQuery.quote(value);
	}

}
