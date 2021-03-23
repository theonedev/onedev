package io.onedev.server.web.page.project.issues;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.util.script.identity.SiteAdministrator;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class ProjectIssuesPage extends ProjectPage implements ScriptIdentityAware {

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
	public ScriptIdentity getScriptIdentity() {
		return new SiteAdministrator();
	}

	@Override
	protected String getPageTitle() {
		return "Issues - " + getProject().getName();
	}
	
}
