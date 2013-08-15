package com.pmease.commons.util.namedentity;

import com.pmease.commons.util.pattern.PatternMatcher;
import com.pmease.commons.util.pattern.WildcardUtils;

public class EntityMatcher implements PatternMatcher {

	private final EntityLoader entityLoader;
	
	private final PatternMatcher nameMatcher;
	
	public EntityMatcher(EntityLoader entityLoader, PatternMatcher nameMatcher) {
		this.entityLoader = entityLoader;
		this.nameMatcher = nameMatcher;
	}
	
	@Override
	public boolean matches(String storedPattern, String input) {
		if (WildcardUtils.hasWildcards(storedPattern)) {
			return nameMatcher.matches(storedPattern, input);
		} else {
			Long id = Long.valueOf(storedPattern);
			NamedEntity identifiable = entityLoader.get(id);
			if (identifiable != null)
				return nameMatcher.matches(identifiable.getName(), input);
			else
				return false;
		}
	}
	
}
