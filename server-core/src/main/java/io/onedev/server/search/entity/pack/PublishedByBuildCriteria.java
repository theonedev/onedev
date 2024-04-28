package io.onedev.server.search.entity.pack;

import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

import javax.annotation.Nullable;
import javax.persistence.criteria.*;

public class PublishedByBuildCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final Build build;

	private final String value;

	public PublishedByBuildCriteria(Build build) {
		this.build = build;
		this.value = build.getReference().toString(null);
	}
	
	public PublishedByBuildCriteria(@Nullable Project project, String value) {
		build = EntityQuery.getBuild(project, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		Path<Build> attribute = from.get(Pack.PROP_BUILD);
		return builder.equal(attribute, build);
	}

	@Override
	public boolean matches(Pack pack) {
		return build.equals(pack.getBuild());
	}

	@Override
	public String toStringWithoutParens() {
		return PackQueryLexer.PublishedByBuild + " " + quote(value);
	}
}
