package io.onedev.server.web.page.project.issues.newissue;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.security.LoginPage;

@SuppressWarnings("serial")
public class NewIssuePage extends ProjectPage implements InputContext {

	public NewIssuePage(PageParameters params) {
		super(params);
		
		User currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private IssueFieldManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Issue issue = new Issue();
		issue.setSubmitter(getLoginUser());
		issue.setSubmitDate(new Date());
		issue.setState(getProject().getIssueWorkflow().getInitialState().getName());
		issue.setProject(getProject());
		
		Serializable fieldBean = getIssueFieldManager().readFields(issue);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				StateSpec stateSpec = Preconditions.checkNotNull(getProject().getIssueWorkflow().getState(issue.getState()));
				getIssueManager().open(issue, fieldBean, stateSpec.getFields());
				setResponsePage(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue));
			}
			
		};
		
		form.add(BeanContext.editBean("builtin", issue));
		
		Set<String> excludedFields = getIssueFieldManager().getExcludedFields(
				getProject(), getProject().getIssueWorkflow().getInitialState().getName());
		form.add(BeanContext.editBean("fields", fieldBean, excludedFields));
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NewIssueResourceReference()));
	}

	@Override
	public List<String> getInputNames() {
		return getProject().getIssueWorkflow().getInputNames();
	}

	@Override
	public InputSpec getInput(String inputName) {
		return getProject().getIssueWorkflow().getInput(inputName);
	}

	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}

}
