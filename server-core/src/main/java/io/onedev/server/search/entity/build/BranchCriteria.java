package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.Constants;

import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.match.WildcardUtils;

public class BranchCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public BranchCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<String> attribute = root.get(Build.PROP_REF_NAME);
		String normalized = Constants.R_HEADS + value.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(Build build) {
		String branch = build.getBranch();
		return branch != null && WildcardUtils.matchString(value.toLowerCase(), branch.toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_BRANCH) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ quote(value);
	}

}
