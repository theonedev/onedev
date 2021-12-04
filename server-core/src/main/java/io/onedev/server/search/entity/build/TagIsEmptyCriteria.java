package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.Constants;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class TagIsEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Build.PROP_REF_NAME);
		return builder.or(
				builder.isNull(attribute), 
				builder.not(builder.like(attribute, Constants.R_TAGS + "%")));
	}

	@Override
	public boolean matches(Build build) {
		return build.getTag() == null;
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_TAG) + " " + BuildQuery.getRuleName(BuildQueryLexer.IsEmpty);
	}

}
