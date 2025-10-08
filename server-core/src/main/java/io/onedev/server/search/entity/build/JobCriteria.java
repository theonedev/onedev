package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class JobCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String jobName;
	
	private final int operator;
	
	public JobCriteria(String jobName, int operator) {
		this.jobName = jobName;
		this.operator = operator;
	}

	public String getJobName() {
		return jobName;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Build.PROP_JOB_NAME);
		String normalized = jobName.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = WildcardUtils.matchString(jobName.toLowerCase(), build.getJobName().toLowerCase());
		if (operator == BuildQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_JOB) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(jobName);
	}

}
