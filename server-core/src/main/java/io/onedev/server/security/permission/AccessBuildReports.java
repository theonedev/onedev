package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;

public class AccessBuildReports implements Permission {

	private final String reportNames;
	
	private transient PatternSet reportNamesPatternSet;
	
	public AccessBuildReports(String reportNames) {
		this.reportNames = reportNames;
	}

	private PatternSet getReportNamesPatternSet() {
		if (reportNamesPatternSet == null)
			reportNamesPatternSet = PatternSet.fromString(reportNames);
		return reportNamesPatternSet;
	}
	@Override
	public boolean implies(Permission p) {
		if (p instanceof AccessBuildReports) {
			AccessBuildReports accessBuildReports = (AccessBuildReports) p;
			return getReportNamesPatternSet().matches(new StringMatcher(), accessBuildReports.reportNames);
		} else {
			return new AccessBuild().implies(p);
		}
	}

}
