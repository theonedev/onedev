package io.onedev.server.web.page.project.pullrequests.detail.operationconfirm;

import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.util.CommitMessageBean;

@SuppressWarnings("serial")
public abstract class MergeConfirmPanel extends OperationConfirmPanel {

	private CommitMessageBean bean = new CommitMessageBean();
	
	public MergeConfirmPanel(String componentId, ModalPanel modal, Long latestUpdateId) {
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
		if (mergeStrategy == CREATE_MERGE_COMMIT) {
			commitMessage = "Merges pull request #" + request.getNumber();
			commitMessage += "\n\n" + request.getTitle();
		} else if (mergeStrategy == SQUASH_SOURCE_BRANCH_COMMITS) {
			commitMessage = request.getTitle();
			if (request.getDescription() != null)
				commitMessage += "\n\n" + request.getDescription();
			commitMessage += "\n\nMerges pull request #" + request.getNumber();
		} else if (mergeStrategy == REBASE_SOURCE_BRANCH_COMMITS) {
			description = "Source branch commits will be rebased onto target branch";
		} else if (mergePreview.getMergeCommitHash().equals(mergePreview.getHeadCommitHash())) {
			description = "Source branch commits will be fast-forwarded to target branch";
		} else {
			commitMessage = "Merges pull request #" + request.getNumber();
			commitMessage += "\n\n" + request.getTitle();
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
		return getPullRequest().getMergeStrategy().toString();
	}

}
