package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitplex.core.branchmatcher.AffinalBranchMatcher;
import com.pmease.gitplex.core.branchmatcher.BranchMatcher;

@SuppressWarnings("serial")
@Editable
@Horizontal
public class IntegrationPolicy implements Serializable {
	
	private BranchMatcher targetBranches;

	private AffinalBranchMatcher sourceBranches;
	
	private List<IntegrationStrategy> integrationStrategies = new ArrayList<>();
	
	@Editable(name="Target Branches", order=100, description="Specify target branches of pull requests to which "
			+ "this policy applies to.")
	@NotNull
	@Valid
	public BranchMatcher getTargetBranches() {
		return targetBranches;
	}

	public void setTargetBranches(BranchMatcher targetBranches) {
		this.targetBranches = targetBranches;
	}

	@Editable(name="Source Branches", order=200, description="Specify source branches of pull requests to which "
			+ "this policy applies to.")
	@NotNull
	@Valid
	public AffinalBranchMatcher getSourceBranches() {
		return sourceBranches;
	}

	public void setSourceBranches(AffinalBranchMatcher sourceBranches) {
		this.sourceBranches = sourceBranches;
	}

	@Editable(order=300, description="<p>Choose one or more integration strategies applicable for pull requests "
			+ "matching branch criterias specified above. User can decide to use one of the applicable "
			+ "strategy to integrate pull request into target branch. Note that the first selected strategy "
			+ "here will appear as default strategy of the pull request. GitPlex for now provides below "
			+ "integration strategies:</p>"
			+ "<dl>"
			+ "<dt>Merge always</dt>"
			+ "<dd>Always create merge commit when integrate into target branch</dd>"
			+ "<dt>Merge if necessary</dt>"
			+ "<dd>Create merge commit only if target branch can not be fast-forwarded to the pull request</dd>"
			+ "<dt>Merge with squash</dt>"
			+ "<dd>Squash all commits in the pull request and then merge with target branch</dd>"
			+ "<dt>Rebase source on top of target</dt>"
			+ "<dd>Rebase source branch on top of target branch and then fast-forward target branch to source branch</dd>"
			+ "<dt>Rebase target on top of source</dt>"
			+ "<dd>Rebase target branch on top of source branch</dd>"
			+ "</dl>")
	@Size(min=1, message="At least one integration strategy should be specified")
	public List<IntegrationStrategy> getIntegrationStrategies() {
		return integrationStrategies;
	}

	public void setIntegrationStrategies(List<IntegrationStrategy> integrationStrategies) {
		this.integrationStrategies = integrationStrategies;
	}

}
