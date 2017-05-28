package com.gitplex.server.web.page.project.pullrequest.requestdetail;

import static com.gitplex.server.model.support.MergeStrategy.ALWAYS_MERGE;
import static com.gitplex.server.model.support.MergeStrategy.SQUASH_MERGE;
import static com.gitplex.server.model.support.MergeStrategy.REBASE_MERGE;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.MergePreview;
import com.gitplex.server.model.support.MergeStrategy;
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
		Project targetProject = request.getTarget().getProject();
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		MergePreview preview = request.getMergePreview();
		MergeStrategy strategy = request.getMergeStrategy();
		boolean sameRepo = request.getTarget().getProject().equals(request.getSource().getProject());					
		if (strategy == SQUASH_MERGE || strategy == REBASE_MERGE) {
			fragment = new Fragment("content", "rebaseInSourceFrag", this);
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
		} else if (user != null 
						&& SecurityUtils.canPush(targetProject, request.getTargetBranch(), request.getTarget().getObjectId(), ObjectId.fromString(preview.getRequestHead()))) {
			fragment = new Fragment("content", "mergeInTargetFrag", this);
			fragment.add(new Label("destRepoName", request.getTarget().getProject()));
			fragment.add(new Label("destBranchNameForCheckout", request.getTargetBranch()));
			fragment.add(new Label("destBranchNameForPush", request.getTargetBranch()));
			WebMarkupContainer sameRepoContainer = new WebMarkupContainer("sameRepo");
			sameRepoContainer.add(new Label("ffOption", strategy==ALWAYS_MERGE?"--no-ff":""));
			sameRepoContainer.add(new Label("srcBranchName", request.getSourceBranch()));
			sameRepoContainer.setVisible(sameRepo);
			fragment.add(sameRepoContainer);
			WebMarkupContainer differentRepoContainer = new WebMarkupContainer("differentRepo");
			differentRepoContainer.add(new Label("ffOption", strategy==ALWAYS_MERGE?"--no-ff":""));
			differentRepoContainer.add(new Label("srcRepoUrl", request.getSource().getProject().getUrl()));
			differentRepoContainer.add(new Label("srcBranchName", request.getSourceBranch()));
			differentRepoContainer.setVisible(!sameRepo);
			fragment.add(differentRepoContainer);
		} else {
			fragment = new Fragment("content", "mergeInSourceFrag", this);
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
