package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;

public class JobCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	public JobCriteria(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder, User user) {
		Path<?> attribute = root.get(BuildConstants.ATTR_JOB);
		return builder.equal(attribute, jobName);
	}

	@Override
	public boolean matches(Build build, User user) {
		return build.getJobName().equals(jobName);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_JOB) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ BuildQuery.quote(jobName);
	}

}
