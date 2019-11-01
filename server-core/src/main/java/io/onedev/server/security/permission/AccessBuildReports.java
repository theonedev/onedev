package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;

public class AccessBuildReports implements Permission {

	private final String reportNames;
	
	public AccessBuildReports(String reportNames) {
		this.reportNames = reportNames;
	}

	@Override
	public boolean implies(Permission p) {
		if (p instanceof AccessBuildReports) {
			AccessBuildReports accessBuildReports = (AccessBuildReports) p;
			return PatternSet.fromString(reportNames).matches(new StringMatcher(), accessBuildReports.reportNames);
		} else {
			return new AccessBuild().implies(p);
		}
	}

}
