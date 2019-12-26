package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;

public class JobPermission implements Permission {

	private final String jobNames;
	
	private final Permission privilege;
	
	private transient PatternSet jobNamesPatternSet;
	
	public JobPermission(String jobNames, Permission privilege) {
		this.jobNames = jobNames;
		this.privilege = privilege;
	}
	
	private PatternSet getJobNamesPatternSet() {
		if (jobNamesPatternSet == null)
			jobNamesPatternSet = PatternSet.parse(jobNames);
		return jobNamesPatternSet;
	}
	@Override
	public boolean implies(Permission p) {
		if (p instanceof JobPermission) {
			JobPermission jobPermission = (JobPermission) p;
			return getJobNamesPatternSet().matches(new StringMatcher(), jobPermission.jobNames) 
					&& privilege.implies(jobPermission.privilege);
		} 
		return false;
	}

}
