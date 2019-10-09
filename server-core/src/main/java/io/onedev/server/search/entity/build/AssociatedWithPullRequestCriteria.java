package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;

public class AssociatedWithPullRequestCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private PullRequest value;
	
	public AssociatedWithPullRequestCriteria(PullRequest value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Build> root, CriteriaBuilder builder, User user) {
		From<?, ?> join = root.join(BuildConstants.ATTR_PULL_REQUEST_BUILDS, JoinType.LEFT);
		return builder.equal(join.get(PullRequestBuild.ATTR_REQUEST), value); 
	}

	@Override
	public boolean matches(Build build, User user) {
		return build.getPullRequestBuilds().stream().anyMatch(it -> it.getRequest().equals(value) && it.isRequired());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.AssociatedWithPullRequest) + " " + BuildQuery.quote("#" + value.getNumber());
	}

}
