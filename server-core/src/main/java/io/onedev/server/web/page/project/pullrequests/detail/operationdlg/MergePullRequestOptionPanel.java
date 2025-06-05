package io.onedev.server.web.page.project.pullrequests.detail.operationdlg;

import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.web.util.editbean.CommitMessageBean;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;

public abstract class MergePullRequestOptionPanel extends ObsoleteUpdateAwarePanel {

	private CommitMessageBean bean = new CommitMessageBean();
	
	public MergePullRequestOptionPanel(String componentId, ModalPanel modal, Long latestUpdateId) {
		super(componentId, modal, latestUpdateId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();

		String commitMessage = null;
		String description = null;
		MergeStrategy mergeStrategy = request.getMergeStrategy();
		MergePreview mergePreview = request.checkMergePreview();
		if (mergeStrategy == CREATE_MERGE_COMMIT || mergeStrategy == SQUASH_SOURCE_BRANCH_COMMITS) {
			commitMessage = request.getAutoMerge().getCommitMessage();
			if (commitMessage == null)
				commitMessage = request.getDefaultMergeCommitMessage();
		} else if (mergeStrategy == REBASE_SOURCE_BRANCH_COMMITS) {
			description = _T("Source branch commits will be rebased onto target branch");
		} else if (mergePreview.getMergeCommitHash().equals(mergePreview.getHeadCommitHash())) {
			description = _T("Target branch will be fast-forwarded to source branch");
		} else {
			commitMessage = request.getDefaultMergeCommitMessage();
		}
		
		getForm().add(new Label("description", description).setVisible(description != null));

		if (commitMessage != null) {
			bean.setCommitMessage(commitMessage);
			getForm().add(BeanContext.edit("commitMessage", bean));
		} else {
			getForm().add(new WebMarkupContainer("commitMessage").setVisible(false));
		}
	}

	private PullRequest getPullRequest() {
		return getLatestUpdate().getRequest();
	}
	
	public String getCommitMessage() {
		return bean.getCommitMessage();
	}

	@Override
	protected String getTitle() {
		return _T(getPullRequest().getMergeStrategy().toString());
	}

}
