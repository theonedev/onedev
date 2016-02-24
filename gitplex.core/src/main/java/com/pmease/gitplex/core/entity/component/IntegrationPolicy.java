package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Horizontal;
import com.pmease.gitplex.core.annotation.BranchMatch;
import com.pmease.gitplex.core.annotation.FullBranchMatch;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchUtils;

@SuppressWarnings("serial")
@Editable
@Horizontal
public class IntegrationPolicy implements Serializable {
	
	private String targetBranchMatch;

	private String sourceBranchMatch;
	
	private List<IntegrationStrategy> integrationStrategies = new ArrayList<>();
	
	@Editable(name="Target Branches", order=100, description="Specify target branches of pull requests to which "
			+ "this policy applies to.")
	@BranchMatch
	@NotEmpty
	public String getTargetBranchMatch() {
		return targetBranchMatch;
	}

	public void setTargetBranchMatch(String targetBranchMatch) {
		this.targetBranchMatch = targetBranchMatch;
	}

	@Editable(name="Source Branches", order=200, description="Specify source branches of pull requests to which "
			+ "this policy applies to.")
	@FullBranchMatch
	@NotEmpty
	public String getSourceBranchMatch() {
		return sourceBranchMatch;
	}

	public void setSourceBranchMatch(String sourceBranchMatch) {
		this.sourceBranchMatch = sourceBranchMatch;
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
	
	public boolean onDepotRename(User depotOwner, String oldName, String newName) {
		String sourceBranchMatch = FullBranchMatchUtils.updateDepotName(this.sourceBranchMatch, depotOwner, oldName, newName);
		if (sourceBranchMatch.equals(this.sourceBranchMatch)) {
			this.sourceBranchMatch = sourceBranchMatch;
			return true;
		} else {
			return false;
		}
	}
	
	public void onAccountRename(String oldName, String newName) {
		sourceBranchMatch = FullBranchMatchUtils.updateAccountName(sourceBranchMatch, oldName, newName);
	}

	@Nullable
	public IntegrationPolicy onDepotDelete(Depot depot) {
		return null;
	}
	
}
