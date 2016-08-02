package com.pmease.gitplex.core.entity.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Horizontal;
import com.pmease.gitplex.core.annotation.BranchMatch;
import com.pmease.gitplex.core.annotation.FullBranchMatch;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchUtils;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeParser;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.CriteriaContext;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.FullBranchMatchContext;

@Editable
@Horizontal
public class IntegrationPolicy implements Serializable {
	
	private static final long serialVersionUID = 1L;

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
	
	public boolean onAccountDelete(String accountName) {
		StringBuilder builder = new StringBuilder();
		for (CriteriaContext criteriaContext: FullBranchMatchUtils.parse(sourceBranchMatch).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				String deleted = deleteAccount(accountName, criteriaContext.includeMatch().fullBranchMatch()); 
				if (deleted != null)
					builder.append(String.format("include(%s) ", deleted));
			} else {
				String deleted = deleteAccount(accountName, criteriaContext.excludeMatch().fullBranchMatch()); 
				if (deleted != null)
					builder.append(String.format("exclude(%s) ", deleted));
			}
		}
		sourceBranchMatch = builder.toString().trim();
		return sourceBranchMatch.length() == 0;
	}
	
	private String deleteAccount(String accountName, FullBranchMatchContext fullBranchMatchContext) {
		if (fullBranchMatchContext.fullDepotMatch() != null) {
			String fullDepotMatch = StringUtils.deleteWhitespace(fullBranchMatchContext.fullDepotMatch().getText());
			String accountMatch = StringUtils.substringBeforeLast(fullDepotMatch, Depot.FQN_SEPARATOR);
			if (accountMatch.equals(accountName)) { 
				return null;
			}
		}
		return fullBranchMatchContext.getText();
	}

	public boolean onBranchDelete(Depot depotDefiningPolicy, Depot depotContainingBranch, String branch) {
		StringBuilder builder = new StringBuilder();
		if (depotDefiningPolicy.equals(depotContainingBranch)) {
			for (IncludeExcludeParser.CriteriaContext criteriaContext: IncludeExcludeUtils.parse(targetBranchMatch).criteria()) {
				if (criteriaContext.includeMatch() != null) { 
					String value = IncludeExcludeUtils.getValue(criteriaContext.includeMatch().Value());
					if (!value.equals(branch))
						builder.append(String.format("include(%s) ", value));
				} else {
					String value = IncludeExcludeUtils.getValue(criteriaContext.excludeMatch().Value());
					if (!value.equals(branch))
						builder.append(String.format("exclude(%s) ", value));
				}
			}
			targetBranchMatch = builder.toString().trim();
			if (targetBranchMatch.length() == 0)
				return true;
		}
		
		builder = new StringBuilder();
		for (CriteriaContext criteriaContext: FullBranchMatchUtils.parse(sourceBranchMatch).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				String deleted = deleteSourceBranch(depotDefiningPolicy, depotContainingBranch, 
						branch, criteriaContext.includeMatch().fullBranchMatch()); 
				if (deleted != null)
					builder.append(String.format("include(%s) ", deleted));
			} else {
				String deleted = deleteSourceBranch(depotDefiningPolicy, depotContainingBranch, 
						branch, criteriaContext.excludeMatch().fullBranchMatch()); 
				if (deleted != null)
					builder.append(String.format("exclude(%s) ", deleted));
			}
		}
		sourceBranchMatch = builder.toString().trim();
		
		return sourceBranchMatch.length() == 0;
	}
	
	private String deleteSourceBranch(Depot depotDefiningPolicy, Depot depotContainingBranch, 
			String branch, FullBranchMatchContext fullBranchMatchContext) {
		DepotAndRevision depotAndRevision = new DepotAndRevision(depotContainingBranch, branch);
		String fullBranchMatch = StringUtils.deleteWhitespace(fullBranchMatchContext.getText());
		if (fullBranchMatch.equals(depotAndRevision.getFQN()) 
				|| depotContainingBranch.equals(depotDefiningPolicy) && branch.equals(fullBranchMatch)) {
			return null;
		} else {
			return fullBranchMatchContext.getText();
		}
	}
	
	public boolean onDepotDelete(Depot depot) {
		StringBuilder builder = new StringBuilder();
		for (CriteriaContext criteriaContext: FullBranchMatchUtils.parse(sourceBranchMatch).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				String deleted = deleteDepot(depot, criteriaContext.includeMatch().fullBranchMatch()); 
				if (deleted != null)
					builder.append(String.format("include(%s) ", deleted));
			} else {
				String deleted = deleteDepot(depot, criteriaContext.excludeMatch().fullBranchMatch()); 
				if (deleted != null)
					builder.append(String.format("exclude(%s) ", deleted));
			}
		}
		sourceBranchMatch = builder.toString().trim();
		return sourceBranchMatch.length() == 0;
	}
	
	private String deleteDepot(Depot depot, FullBranchMatchContext fullBranchMatchContext) {
		if (fullBranchMatchContext.fullDepotMatch() != null) {
			String fullDepotMatch = StringUtils.deleteWhitespace(fullBranchMatchContext.fullDepotMatch().getText());
			if (fullDepotMatch.equals(depot.getFQN())) { 
				return null;
			}
		}
		return fullBranchMatchContext.getText();
	}
	
	public void onDepotTransfer(Depot transferredDepot, Account originalAccount) {
		StringBuilder builder = new StringBuilder();
		for (CriteriaContext criteriaContext: FullBranchMatchUtils.parse(sourceBranchMatch).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				String updated = updateDepotAccount(transferredDepot.getName(), 
						originalAccount.getName(), transferredDepot.getAccount().getName(), 
						criteriaContext.includeMatch().fullBranchMatch()); 
				builder.append(String.format("include(%s) ", updated));
			} else {
				String updated = updateDepotAccount(transferredDepot.getName(), 
						originalAccount.getName(), transferredDepot.getAccount().getName(), 
						criteriaContext.excludeMatch().fullBranchMatch()); 
				builder.append(String.format("exclude(%s) ", updated));
			}
		}
		sourceBranchMatch = builder.toString().trim();
	}
	
	private static String updateDepotAccount(String depotName, String oldAccountName, String newAccountName, 
			FullBranchMatchContext fullBranchMatchContext) {
		if (fullBranchMatchContext.fullDepotMatch() != null) {
			String fullDepotMatch = StringUtils.deleteWhitespace(fullBranchMatchContext.fullDepotMatch().getText());
			if (fullDepotMatch.equals(oldAccountName + Depot.FQN_SEPARATOR + depotName)) { 
				return newAccountName
						+ Depot.FQN_SEPARATOR 
						+ depotName 
						+ DepotAndBranch.SEPARATOR 
						+ fullBranchMatchContext.branchMatch().getText().trim();
			}
		}
		return fullBranchMatchContext.getText();
	}
	
	public void onDepotRename(Account depotAccount, String oldName, String newName) {
		StringBuilder builder = new StringBuilder();
		for (CriteriaContext criteriaContext: FullBranchMatchUtils.parse(sourceBranchMatch).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				String updated = updateDepotName(depotAccount, oldName, newName, 
						criteriaContext.includeMatch().fullBranchMatch()); 
				builder.append(String.format("include(%s) ", updated));
			} else {
				String updated = updateDepotName(depotAccount, oldName, newName, 
						criteriaContext.excludeMatch().fullBranchMatch()); 
				builder.append(String.format("exclude(%s) ", updated));
			}
		}
		sourceBranchMatch = builder.toString().trim();
	}
	
	private String updateDepotName(Account depotAccount, String oldName, String newName, 
			FullBranchMatchContext fullBranchMatchContext) {
		if (fullBranchMatchContext.fullDepotMatch() != null) {
			String fullDepot = StringUtils.deleteWhitespace(fullBranchMatchContext.fullDepotMatch().getText());
			if (fullDepot.equals(depotAccount.getName() + Depot.FQN_SEPARATOR + oldName)) { 
				return depotAccount.getName() 
						+ Depot.FQN_SEPARATOR 
						+ newName 
						+ DepotAndBranch.SEPARATOR 
						+ fullBranchMatchContext.branchMatch().getText().trim();
			}
		}
		return fullBranchMatchContext.getText();
	}

	public void onAccountRename(String oldName, String newName) {
		StringBuilder builder = new StringBuilder();
		for (CriteriaContext criteriaContext: FullBranchMatchUtils.parse(sourceBranchMatch).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				String updated = updateAccountName(oldName, newName, 
						criteriaContext.includeMatch().fullBranchMatch()); 
				builder.append(String.format("include(%s) ", updated));
			} else {
				String updated = updateAccountName(oldName, newName, 
						criteriaContext.excludeMatch().fullBranchMatch()); 
				builder.append(String.format("exclude(%s) ", updated));
			}
		}
		sourceBranchMatch = builder.toString().trim();
	}
	
	private static String updateAccountName(String oldName, String newName, 
			FullBranchMatchContext fullBranchMatchContext) {
		if (fullBranchMatchContext.fullDepotMatch() != null) {
			String fullDepotMatch = StringUtils.deleteWhitespace(fullBranchMatchContext.fullDepotMatch().getText());
			String accountMatch = StringUtils.substringBeforeLast(fullDepotMatch, Depot.FQN_SEPARATOR);
			if (accountMatch.equals(oldName)) { 
				return newName 
						+ Depot.FQN_SEPARATOR 
						+ StringUtils.substringAfterLast(fullDepotMatch, Depot.FQN_SEPARATOR) 
						+ DepotAndBranch.SEPARATOR 
						+ fullBranchMatchContext.branchMatch().getText().trim();
			}
		}
		return fullBranchMatchContext.getText();
	}
	
}
