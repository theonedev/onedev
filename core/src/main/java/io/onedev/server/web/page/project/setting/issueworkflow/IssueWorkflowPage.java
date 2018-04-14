package io.onedev.server.web.page.project.setting.issueworkflow;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class IssueWorkflowPage extends ProjectSettingPage {

	private final IssueWorkflow workflow;
	
	public IssueWorkflowPage(PageParameters params) {
		super(params);
		
		workflow = getProject().getIssueWorkflow();
	}

	public IssueWorkflow getWorkflow() {
		return workflow;
	}

}
