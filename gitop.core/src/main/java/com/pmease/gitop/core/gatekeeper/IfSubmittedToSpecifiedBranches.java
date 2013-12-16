package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.EntityMatcher;
import com.pmease.commons.util.namedentity.EntityPatternSet;
import com.pmease.commons.util.pattern.PatternSetMatcher;
import com.pmease.commons.util.pattern.WildcardPathMatcher;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.BranchGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=100, icon="icon-git-branch", description=
		"This gate keeper will be passed if the commit is submitted to specified branches.")
public class IfSubmittedToSpecifiedBranches extends BranchGateKeeper {

	private String branchIds;
	
	@Editable
	@NotNull
	public String getBranchIds() {
		return branchIds;
	}

	public void setBranchIds(String branchIds) {
		this.branchIds = branchIds;
	}

	@Override
	public CheckResult doCheck(PullRequest request) {
		Project project = request.getTarget().getProject();
		BranchManager branchManager = AppLoader.getInstance(BranchManager.class);
		EntityLoader entityLoader = branchManager.asEntityLoader(project);
		EntityMatcher entityMatcher = new EntityMatcher(entityLoader, new WildcardPathMatcher());
		PatternSetMatcher patternSetMatcher = new PatternSetMatcher(entityMatcher);

		EntityPatternSet patternSet = EntityPatternSet.fromStored(getBranchIds(), entityLoader);

		if (patternSetMatcher.matches(getBranchIds(), request.getTarget().getName()))
			return accepted("Target branch matches pattern '" + patternSet + "'.");
		else
			return rejected("Target branch does not match pattern '" + patternSet + "'.");
	}

	@Override
	public Object trim(Object context) {
		Preconditions.checkArgument(context instanceof Project);
		
		Project project = (Project) context;
		BranchManager branchManager = AppLoader.getInstance(BranchManager.class);
		EntityLoader entityLoader = branchManager.asEntityLoader(project);
		EntityPatternSet patternSet = EntityPatternSet.fromStored(getBranchIds(), entityLoader);
		patternSet.trim(project);
		
		if (patternSet.getStored().isEmpty()) {
			return null;
		} else {
			setBranchIds(patternSet.toString());
			return this;
		}
	}

}
