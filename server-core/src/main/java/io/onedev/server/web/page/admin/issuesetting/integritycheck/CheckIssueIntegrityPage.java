package io.onedev.server.web.page.admin.issuesetting.integritycheck;

import io.onedev.server.web.component.issue.workflowreconcile.WorkflowReconcilePanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class CheckIssueIntegrityPage extends IssueSettingPage {
	public CheckIssueIntegrityPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("run") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new WorkflowReconcilePanel(id) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onCompleted(AjaxRequestTarget target) {
						setResponsePage(CheckIssueIntegrityPage.class);
					}

				};
			}

		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Check Issue Integrity");
	}
}
