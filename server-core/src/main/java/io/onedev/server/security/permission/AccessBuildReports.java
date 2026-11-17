package io.onedev.server.security.permission;

import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import org.apache.shiro.authz.Permission;

public class AccessBuildReports implements BasePermission {

	private final String reportNames;
	
	private transient PatternSet reportPatterns;
	
	public AccessBuildReports(String reportNames) {
		this.reportNames = reportNames;
	}

	private PatternSet getReportPatterns() {
		if (reportPatterns == null)
			reportPatterns = PatternSet.parse(reportNames);
		return reportPatterns;
	}
	@Override
	public boolean implies(Permission p) {
		if (p instanceof AccessBuildReports) {
			AccessBuildReports accessBuildReports = (AccessBuildReports) p;
			return getReportPatterns().matches(new StringMatcher(), accessBuildReports.reportNames);
		} else {
			return new AccessProject().implies(p);
		}
	}
}
