package io.onedev.server.web.page.project.pullrequests.detail.operationconfirm;

import io.onedev.server.web.page.project.pullrequests.detail.CommitMessageBean;
import org.apache.wicket.markup.html.WebMarkupContainer;

import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
public abstract class UpdateSourceBranchConfirmPanel extends OperationConfirmPanel {

	private CommitMessageBean bean = new CommitMessageBean();

	public UpdateSourceBranchConfirmPanel(String componentId, ModalPanel modal, Long latestUpdateId) {
		super(componentId, modal, latestUpdateId);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequest request = getPullRequest();
		String commitMessage = request.getDefaultUpdateSourceBranchCommitMessage();
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

}
