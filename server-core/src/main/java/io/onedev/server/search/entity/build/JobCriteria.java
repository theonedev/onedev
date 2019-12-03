package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.BuildQueryConstants;

public class JobCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	public JobCriteria(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder, User user) {
		Path<String> attribute = root.get(BuildQueryConstants.ATTR_JOB);
		String normalized = jobName.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(Build build, User user) {
		return WildcardUtils.matchString(jobName.toLowerCase(), build.getJobName().toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildQueryConstants.FIELD_JOB) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ BuildQuery.quote(jobName);
	}

}
