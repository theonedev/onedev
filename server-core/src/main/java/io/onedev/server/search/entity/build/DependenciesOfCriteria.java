package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;

public class DependenciesOfCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private Build value;
	
	public DependenciesOfCriteria(Build value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Build> root, CriteriaBuilder builder, User user) {
		From<?, ?> join = root.join(BuildConstants.ATTR_DEPENDENTS, JoinType.LEFT);
		return builder.equal(join.get(BuildDependence.ATTR_DEPENDENT), value);
	}

	@Override
	public boolean matches(Build build, User user) {
		for (BuildDependence dependence: value.getDependencies()) {
			if (dependence.getDependency().equals(build))
				return true;
		}
		return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.getRuleName(BuildQueryLexer.DependenciesOf) + " " + BuildQuery.quote("#" + value.getNumber());
	}

}
