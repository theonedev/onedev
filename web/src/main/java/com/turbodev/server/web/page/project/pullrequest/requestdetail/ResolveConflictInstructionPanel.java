package com.turbodev.server.web.page.project.pullrequest.requestdetail;

import static com.turbodev.server.model.support.MergeStrategy.ALWAYS_MERGE;
import static com.turbodev.server.model.support.MergeStrategy.REBASE_MERGE;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.support.MergeStrategy;

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
		MergeStrategy strategy = request.getMergeStrategy();
		boolean sameRepo = request.getTarget().getProject().equals(request.getSource().getProject());					
		if (strategy == REBASE_MERGE) {
			fragment = new Fragment("content", "rebaseFrag", this);
			fragment.add(new Label("srcRepoName", request.getSource().getProject()));
			fragment.add(new Label("srcBranchNameForCheckout", request.getSourceBranch()));
			fragment.add(new Label("srcBranchNameForPush", request.getSourceBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("destRepoUrl", request.getTarget().getProject().getUrl()));
			differentRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		} else {
			fragment = new Fragment("content", "mergeFrag", this);
			fragment.add(new Label("srcRepoName", request.getSource().getProject()));
			fragment.add(new Label("srcBranchNameForCheckout", request.getSourceBranch()));
			fragment.add(new Label("srcBranchNameForPush", request.getSourceBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("ffOption", strategy==ALWAYS_MERGE?"--no-ff":""));
			sameRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("ffOption", strategy==ALWAYS_MERGE?"--no-ff":""));
			differentRepoContainer.add(new Label("destRepoUrl", request.getTarget().getProject().getUrl()));
			differentRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		}
		
		add(fragment);
	}

}
