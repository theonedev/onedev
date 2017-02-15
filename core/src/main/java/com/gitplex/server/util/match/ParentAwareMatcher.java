package com.gitplex.server.util.match;

/**
 * Besides the normal child aware match, this matcher also matches
 * parent path ending with slash. For instance, if pattern is defined
 * as &quot;parent/child&quot;, it will match value &quot;parent/&quot;. 
 * A scenario using this matcher: 
 * <ol>
 * <li> A gatekeeper is defined to require that all created tags should match 
 * the format &quot;refs/tags/v*&quot;
 * <li> The tag creation link checks the gatekeeper using ref value &quot;refs/tags/&quot;
 * to see if tag creation is allowed, and it matches meaning one can create tag
 * <li> User then input tag name, and this name will be matched again with the gatekeeper
 * and if it does not starts with letter &quot;v&quot;, the tag creation will not be allowed
 * 
 * @author robin
 *
 */
public class ParentAwareMatcher extends ChildAwareMatcher {

	@Override
	public boolean matches(String rule, String value) {
		if (rule.startsWith(value) && value.endsWith("/")) {
			return true;
		} else {
			return super.matches(rule, value);
		}
	}

}
