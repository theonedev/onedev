package io.onedev.server.security.permission;

import io.onedev.server.util.facade.UserFacade;
import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

public class AccessSites implements BasePermission {

	private final String siteNames;
	
	private transient PatternSet sitePatterns;
	
	public AccessSites(String siteNames) {
		this.siteNames = siteNames;
	}

	private PatternSet getSitePatterns() {
		if (sitePatterns == null)
			sitePatterns = PatternSet.parse(siteNames);
		return sitePatterns;
	}
	@Override
	public boolean implies(Permission p) {
		if (p instanceof AccessSites) {
			AccessSites accessSites = (AccessSites) p;
			return getSitePatterns().matches(new StringMatcher(), accessSites.siteNames);
		} else {
			return new AccessProject().implies(p);
		}
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return true;
	}
}
