package com.pmease.gitop.core.model.gatekeeper;

import com.google.common.base.Preconditions;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.EntityMatcher;
import com.pmease.commons.util.namedentity.EntityPatternSet;
import com.pmease.commons.util.pattern.PatternSetMatcher;
import com.pmease.commons.util.pattern.WildcardStringMatcher;
import com.pmease.gitop.core.entitymanager.TeamManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.Repository;
import com.pmease.gitop.core.model.TeamMembership;

public class AllowSpecifiedTeams implements GateKeeper {

	private String teamPatterns;
	
	public String getTeamPatterns() {
		return teamPatterns;
	}

	public void setTeamPatterns(String teamPatterns) {
		this.teamPatterns = teamPatterns;
	}

	@Override
	public CheckResult check(MergeRequest mergeRequest) {
		Repository repository = mergeRequest.getTargetBranch().getRepository();
		EntityMatcher entityMatcher = new EntityMatcher(getEntityLoader(repository), new WildcardStringMatcher());
		PatternSetMatcher patternSetMatcher = new PatternSetMatcher(entityMatcher);
		
		for (TeamMembership each: mergeRequest.getUser().getTeamMemberships()) {
			if (patternSetMatcher.matches(getTeamPatterns(), each.getTeam().getName()))
				return CheckResult.ACCEPT;
		}
		
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
			setTeamPatterns(patternSet.toString());
			return this;
		}
	}

	private EntityLoader getEntityLoader(Repository repository) {
		TeamManager teamManager = AppLoader.getInstance(TeamManager.class);
		return teamManager.asEntityLoader(repository.getOwner());
	}
	
	private EntityPatternSet asEntityPatternSet(Repository repository) {
		EntityLoader entityLoader = getEntityLoader(repository);
		return EntityPatternSet.fromStored(teamPatterns, entityLoader);
	}

}
