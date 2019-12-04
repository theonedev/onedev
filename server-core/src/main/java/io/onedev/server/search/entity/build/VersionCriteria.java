package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Build;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.BuildQueryConstants;

public class VersionCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public VersionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<String> attribute = root.get(BuildQueryConstants.ATTR_VERSION);
		String normalized = value.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(Build build) {
		String version = build.getVersion();
		return version != null && WildcardUtils.matchString(value.toLowerCase(), version.toLowerCase());
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildQueryConstants.FIELD_VERSION) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ BuildQuery.quote(value);
	}

}
