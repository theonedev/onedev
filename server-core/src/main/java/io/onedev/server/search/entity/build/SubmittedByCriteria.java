package io.onedev.server.search.entity.build;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;

public class SubmittedByCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public SubmittedByCriteria(User user) {
		this.user = user;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<User> attribute = root.get(Build.PROP_SUBMITTER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(Build build) {
		return Objects.equals(build.getSubmitter(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.SubmittedBy) + " " + quote(user.getName());
	}

}
