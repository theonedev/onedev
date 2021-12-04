package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class JobCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	public JobCriteria(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Build.PROP_JOB);
		String normalized = jobName.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(Build build) {
		return WildcardUtils.matchString(jobName.toLowerCase(), build.getJobName().toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_JOB) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ quote(jobName);
	}

}
