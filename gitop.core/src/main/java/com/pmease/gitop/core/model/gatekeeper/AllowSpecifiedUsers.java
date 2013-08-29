package com.pmease.gitop.core.model.gatekeeper;

import com.google.common.base.Preconditions;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.EntityMatcher;
import com.pmease.commons.util.namedentity.EntityPatternSet;
import com.pmease.commons.util.pattern.PatternSetMatcher;
import com.pmease.commons.util.pattern.WildcardStringMatcher;
import com.pmease.gitop.core.entitymanager.UserManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.Repository;

public class AllowSpecifiedUsers implements GateKeeper {

	private String userPatterns;
	
	public String getUserPatterns() {
		return userPatterns;
	}

	public void setUserPatterns(String userPatterns) {
		this.userPatterns = userPatterns;
	}

	@Override
	public CheckResult check(MergeRequest mergeRequest) {
		Repository repository = mergeRequest.getTargetRepository();
		EntityMatcher entityMatcher = new EntityMatcher(getEntityLoader(repository), new WildcardStringMatcher());
		PatternSetMatcher patternSetMatcher = new PatternSetMatcher(entityMatcher);
		
		if (patternSetMatcher.matches(getUserPatterns(), mergeRequest.getUser().getName()))
			return CheckResult.ACCEPT;
		else 
			return CheckResult.REJECT;
	}

	@Override
	public Object trim(Object context) {
		Preconditions.checkArgument(context instanceof Repository);
		
		Repository repository = (Repository) context;
		EntityPatternSet patternSet = asEntityPatternSet(repository);
		patternSet.trim(repository);
		
		if (patternSet.getStored().isEmpty()) {
			return null;
		} else {
			setUserPatterns(patternSet.toString());
			return this;
		}
	}

	private EntityLoader getEntityLoader(Repository repository) {
		UserManager userManager = AppLoader.getInstance(UserManager.class);
		return userManager.asEntityLoader();
	}
	
	private EntityPatternSet asEntityPatternSet(Repository repository) {
		EntityLoader entityLoader = getEntityLoader(repository);
		return EntityPatternSet.fromStored(userPatterns, entityLoader);
	}

}
