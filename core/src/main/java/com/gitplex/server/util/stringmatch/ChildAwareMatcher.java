package com.gitplex.server.util.stringmatch;

/**
 * Besides the normal wildcard string match, this matcher also matches 
 * paths beneath a parent path if the parent path ends with slash. For 
 * instance, if the pattern is &quot;parent/&quot;, it will match value 
 * &quot;parent/child&quot;
 * 
 * @author robin
 *
 */
public class ChildAwareMatcher extends WildcardStringMatcher {

	@Override
	public boolean matches(String rule, String value) {
		if (value.startsWith(rule) && rule.endsWith("/")) {
			return true;
		} else {
			return super.matches(rule, value);
		}
	}

}
