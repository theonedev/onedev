package io.onedev.server.web.page.project.pullrequests.detail.operationconfirm;

import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.pullrequests.detail.CommitMessageBean;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.modal.ModalPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;

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
        String commitMessage = request.getDefaultUpdateSourceBranchCommitMessage(MergeStrategy.CREATE_MERGE_COMMIT);
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
