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
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;

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

	@Editable(order=300, descriptionProvider="getIntegrationStrategyHelp")
	@Size(min=1, message="At least one integration strategy should be specified")
	public List<IntegrationStrategy> getIntegrationStrategies() {
		return integrationStrategies;
	}

	public void setIntegrationStrategies(List<IntegrationStrategy> integrationStrategies) {
		this.integrationStrategies = integrationStrategies;
	}

	@SuppressWarnings("unused")
	private static String getIntegrationStrategyHelp() {
		StringBuilder help = new StringBuilder("<p>Choose one or more integration strategies applicable for pull requests "
				+ "matching branch criterias specified above. User can decide to use one of the applicable "
				+ "strategy to integrate pull request into target branch. Note that the first selected strategy "
				+ "here will appear as default strategy of the pull request. GitPlex for now provides below "
				+ "integration strategies:</p>");
		
		help.append("<dl>");
		
		for (IntegrationStrategy strategy: IntegrationStrategy.values()) {
			help.append("<dt>").append(strategy.toString()).append("</dt>");
			help.append("<dd>").append(strategy.getDescription()).append("</dd>");
		}

		help.append("</dl>");
		
		return help.toString();
	}
}
