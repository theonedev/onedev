package io.onedev.server.security.permission;

import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import org.apache.shiro.authz.Permission;

public class JobPermission implements BasePermission {

	private final String jobNames;
	
	private final BasePermission privilege;
	
	private transient PatternSet jobNamesPatternSet;
	
	public JobPermission(String jobNames, BasePermission privilege) {
		this.jobNames = jobNames;
		this.privilege = privilege;
	}

	public Permission getPrivilege() {
		return privilege;
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
			return (jobPermission.jobNames == null || getJobNamesPatternSet().matches(new StringMatcher(), jobPermission.jobNames)) 
					&& privilege.implies(jobPermission.privilege);
		} 
		return false;
	}

	@Override
	public boolean isWrite() {
		return privilege.isWrite();
	}
}
