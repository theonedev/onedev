package io.onedev.server.search.entity.pack;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PublishedViaBuildCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final Build build;

	private final String value;

	public PublishedViaBuildCriteria(Build build) {
		this.build = build;
		this.value = build.getReference().toString(null);
	}
	
	public PublishedViaBuildCriteria(@Nullable Project project, String value) {
		build = EntityQuery.getBuild(project, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		Path<Build> attribute = from.get(Pack.PROP_BUILD);
		return builder.equal(attribute, build);
	}

	@Override
	public boolean matches(Pack pack) {
		return build.equals(pack.getBuild());
	}

	@Override
	public String toStringWithoutParens() {
		return PackQuery.getRuleName(PackQueryLexer.PublishedByBuild) + " " + quote(value);
	}
}
