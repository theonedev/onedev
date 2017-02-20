package com.gitplex.server.web.page.depot.pullrequest.requestdetail;

import static com.gitplex.server.model.PullRequest.IntegrationStrategy.MERGE_ALWAYS;
import static com.gitplex.server.model.PullRequest.IntegrationStrategy.MERGE_WITH_SQUASH;
import static com.gitplex.server.model.PullRequest.IntegrationStrategy.REBASE_SOURCE_ONTO_TARGET;
import static com.gitplex.server.model.PullRequest.IntegrationStrategy.REBASE_TARGET_ONTO_SOURCE;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequest.IntegrationStrategy;
import com.gitplex.server.model.support.IntegrationPreview;
import com.gitplex.server.security.SecurityUtils;

@SuppressWarnings("serial")
class ResolveConflictInstructionPanel extends Panel {

	public ResolveConflictInstructionPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = (PullRequest) getDefaultModelObject();
		Fragment fragment;
		Depot targetDepot = request.getTarget().getDepot();
		Account user = GitPlex.getInstance(AccountManager.class).getCurrent();
		IntegrationPreview preview = request.getIntegrationPreview();
		IntegrationStrategy strategy = request.getIntegrationStrategy();
		boolean sameRepo = request.getTarget().getDepot().equals(request.getSource().getDepot());					
		if (strategy == MERGE_WITH_SQUASH || strategy == REBASE_SOURCE_ONTO_TARGET) {
			fragment = new Fragment("content", "rebaseInSourceFrag", this);
			fragment.add(new Label("srcRepoName", request.getSource().getDepot()));
			fragment.add(new Label("srcBranchNameForCheckout", request.getSourceBranch()));
			fragment.add(new Label("srcBranchNameForPush", request.getSourceBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("destRepoUrl", request.getTarget().getDepot().getUrl()));
			differentRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		} else if (strategy == REBASE_TARGET_ONTO_SOURCE) {
			fragment = new Fragment("content", "rebaseInTargetFrag", this);
			fragment.add(new Label("destRepoName", request.getTarget().getDepot()));
			fragment.add(new Label("destBranchNameForCheckout", request.getTargetBranch()));
			fragment.add(new Label("destBranchNameForPush", request.getTargetBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("srcBranchName", request.getSourceBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("srcRepoUrl", request.getSource().getDepot().getUrl()));
			differentRepoContainer.add(new Label("srcBranchName", request.getSourceBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		} else if (user != null 
						&& SecurityUtils.canWrite(targetDepot)
						&& targetDepot.getGateKeeper().checkPush(user, request.getTargetDepot(), request.getTargetRef(), request.getTarget().getObjectId(), ObjectId.fromString(preview.getRequestHead())).isPassedOrIgnored()) {
			fragment = new Fragment("content", "mergeInTargetFrag", this);
			fragment.add(new Label("destRepoName", request.getTarget().getDepot()));
			fragment.add(new Label("destBranchNameForCheckout", request.getTargetBranch()));
			fragment.add(new Label("destBranchNameForPush", request.getTargetBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("ffOption", strategy==MERGE_ALWAYS?"--no-ff":""));
			sameRepoContainer.add(new Label("srcBranchName", request.getSourceBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("ffOption", strategy==MERGE_ALWAYS?"--no-ff":""));
			differentRepoContainer.add(new Label("srcRepoUrl", request.getSource().getDepot().getUrl()));
			differentRepoContainer.add(new Label("srcBranchName", request.getSourceBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		} else {
			fragment = new Fragment("content", "mergeInSourceFrag", this);
			fragment.add(new Label("srcRepoName", request.getSource().getDepot()));
			fragment.add(new Label("srcBranchNameForCheckout", request.getSourceBranch()));
			fragment.add(new Label("srcBranchNameForPush", request.getSourceBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("ffOption", strategy==MERGE_ALWAYS?"--no-ff":""));
			sameRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("ffOption", strategy==MERGE_ALWAYS?"--no-ff":""));
			differentRepoContainer.add(new Label("destRepoUrl", request.getTarget().getDepot().getUrl()));
			differentRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		}
		
		add(fragment);
	}

}
