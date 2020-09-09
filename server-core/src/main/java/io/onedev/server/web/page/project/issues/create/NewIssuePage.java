package io.onedev.server.web.page.project.issues.create;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.util.script.identity.SiteAdministrator;
import io.onedev.server.web.component.issue.create.NewIssueEditor;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.simple.security.LoginPage;

@SuppressWarnings("serial")
public class NewIssuePage extends ProjectPage implements InputContext, ScriptIdentityAware {

	private static final String PARAM_TEMPLATE = "query";
	
	private IModel<IssueCriteria> templateModel;
	
	public NewIssuePage(PageParameters params) {
		super(params);
		
		User currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		
		String queryString = params.get(PARAM_TEMPLATE).toString();
		templateModel = new LoadableDetachableModel<IssueCriteria>() {

			@Override
			protected IssueCriteria load() {
				try {
					return IssueQuery.parse(getProject(), queryString, true, true, false, false, false).getCriteria();
				} catch (Exception e) {
					return null;
				}
			}
			
		};
	}

	@Override
	protected void onDetach() {
		templateModel.detach();
		super.onDetach();
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
		NewIssueEditor editor = new NewIssueEditor("newIssue") {

			@Override
			protected Project getProject() {
				return NewIssuePage.this.getProject();
			}

			@Override
			protected IssueCriteria getTemplate() {
				return templateModel.getObject();
			}
			
		};		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				Issue issue = editor.getConvertedInput();
				OneDev.getInstance(IssueManager.class).open(issue);
				setResponsePage(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue));
			}
			
		};
		
		form.add(editor);
		
		add(form);
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Override
	public List<String> getInputNames() {
		return getIssueSetting().getFieldNames();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getIssueSetting().getFieldSpec(inputName);
	}

	@Override
	public ScriptIdentity getScriptIdentity() {
		return new SiteAdministrator();
	}

	public static PageParameters paramsOf(Project project, String template) {
		PageParameters params = paramsOf(project);
		if (template != null)
			params.add(PARAM_TEMPLATE, template);
		return params;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-nowrap'>Create Issue</span>").setEscapeModelStrings(false);
	}
	
}
