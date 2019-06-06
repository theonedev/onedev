package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;

public class CommitCriteria extends EntityCriteria<Build>  {

	private static final long serialVersionUID = 1L;

	private final ObjectId value;
	
	public CommitCriteria(ObjectId value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Build> root, CriteriaBuilder builder, User user) {
		Path<?> attribute = BuildQuery.getPath(root, BuildConstants.ATTR_COMMIT);
		return builder.equal(attribute, value.name());
	}

	@Override
	public boolean matches(Build build, User user) {
		return build.getCommitHash().equals(value.name());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_COMMIT) + " " + BuildQuery.getRuleName(BuildQueryLexer.Is) + " " + BuildQuery.quote(value.name());
	}

}
