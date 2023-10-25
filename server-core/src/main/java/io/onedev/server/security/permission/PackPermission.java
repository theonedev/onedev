package io.onedev.server.security.permission;

import io.onedev.server.model.User;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

public class PackPermission implements BasePermission {

	private final String packNames;
	
	private final BasePermission privilege;
	
	private transient PatternSet packNamesPatternSet;
	
	public PackPermission(String packNames, BasePermission privilege) {
		this.packNames = packNames;
		this.privilege = privilege;
	}
	
	private PatternSet getPackNamesPatternSet() {
		if (packNamesPatternSet == null)
			packNamesPatternSet = PatternSet.parse(packNames);
		return packNamesPatternSet;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof PackPermission) {
			PackPermission packPermission = (PackPermission) p;
			return (packPermission.packNames == null || getPackNamesPatternSet().matches(new StringMatcher(), packPermission.packNames)) 
					&& privilege.implies(packPermission.privilege);
		} 
		return false;
	}

	@Override
	public boolean isApplicable(@Nullable User user) {
		return privilege.isApplicable(user);
	}
	
}
