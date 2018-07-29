package io.onedev.server.web.page.project.issues.newissue;

import java.util.List;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entityquery.issue.IssueCriteria;
import io.onedev.server.entityquery.issue.IssueQuery;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.newissue.NewIssueEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.security.LoginPage;

@SuppressWarnings("serial")
public class NewIssuePage extends ProjectPage implements InputContext {

	private static final String PARAM_QUERY = "query";
	
	private IModel<IssueCriteria> templateModel;
	
	public NewIssuePage(PageParameters params) {
		super(params);
		
		User currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		
		String queryString = params.get(PARAM_QUERY).toString();
		templateModel = new LoadableDetachableModel<IssueCriteria>() {

			@Override
			protected IssueCriteria load() {
				try {
					IssueQuery query = IssueQuery.parse(getProject(), queryString, true);
					return query.getCriteria();
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

		NewIssueEditor editor = new NewIssueEditor("newIssue") {

			@Override
			protected Project getProject() {
				return NewIssuePage.this.getProject();
			}
			
		};		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				Issue issue = editor.getConvertedInput();
				OneDev.getInstance(IssueManager.class).open(issue);
				setResponsePage(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null));
			}
			
		};
		
		form.add(editor);
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NewIssueResourceReference()));
	}

	@Override
	public List<String> getInputNames() {
		return getProject().getIssueWorkflow().getFieldNames();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getProject().getIssueWorkflow().getFieldSpec(inputName);
	}

	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}

	public static PageParameters paramsOf(Project project, String query) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
