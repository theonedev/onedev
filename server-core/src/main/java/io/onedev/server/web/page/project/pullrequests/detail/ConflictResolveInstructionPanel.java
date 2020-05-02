package io.onedev.server.web.page.project.pullrequests.detail;

import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.web.component.project.gitprotocol.GitProtocolPanel;

@SuppressWarnings("serial")
abstract class ConflictResolveInstructionPanel extends Panel {

	public ConflictResolveInstructionPanel(String id) {
		super(id);
	}

	protected abstract PullRequest getPullRequest();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();
		Fragment fragment;
		MergeStrategy strategy = request.getMergeStrategy();
		boolean sameRepo = request.getTarget().getProject().equals(request.getSource().getProject());	
		
		if (strategy == REBASE_SOURCE_BRANCH_COMMITS) {
			fragment = new Fragment("body", "rebaseFrag", this);
			fragment.add(new Label("srcRepoName", request.getSource().getProject()));
			fragment.add(new Label("srcBranchNameForCheckout", request.getSourceBranch()));
			fragment.add(new Label("srcBranchNameForPush", request.getSourceBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("destRepoUrl", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return findParent(GitProtocolPanel.class).getProtocolUrl();
				}
				
			}));
			differentRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		} else {
			fragment = new Fragment("body", "mergeFrag", this);
			fragment.add(new Label("srcRepoName", request.getSource().getProject()));
			fragment.add(new Label("srcBranchNameForCheckout", request.getSourceBranch()));
			fragment.add(new Label("srcBranchNameForPush", request.getSourceBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("ffOption", strategy==CREATE_MERGE_COMMIT?"--no-ff":""));
			sameRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("ffOption", strategy==CREATE_MERGE_COMMIT?"--no-ff":""));
			differentRepoContainer.add(new Label("destRepoUrl", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return findParent(GitProtocolPanel.class).getProtocolUrl();
				}
				
			}));
			differentRepoContainer.add(new Label("destBranchName", request.getTargetBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		}
		
		add(fragment);
	}

}
