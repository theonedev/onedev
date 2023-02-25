package io.onedev.server.web.page.project.issues;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.onedev.server.web.page.project.ProjectPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public abstract class ProjectIssuesPage extends ProjectPage {

	public ProjectIssuesPage(PageParameters params) {
		super(params);
	}

	protected GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WorkflowChangeAlertPanel("workflowChangeAlert") {

			@Override
			protected void onCompleted(AjaxRequestTarget target) {
				setResponsePage(getPageClass(), getPageParameters());
			}
			
		});
	}

	@Override
	protected String getPageTitle() {
		return "Issues - " + getProject().getPath();
	}
	
}
