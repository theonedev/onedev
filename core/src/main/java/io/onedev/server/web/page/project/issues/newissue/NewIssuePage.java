package io.onedev.server.web.page.project.issues.newissue;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.security.LoginPage;

@SuppressWarnings("serial")
public class NewIssuePage extends ProjectPage implements InputContext {

	private static final String PARAM_QUERY = "query";
	
	private String queryString;
	
	private String milestoneName;
	
	public NewIssuePage(PageParameters params) {
		super(params);
		
		User currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		
		queryString = params.get(PARAM_QUERY).toString();
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private IssueFieldUnaryManager getIssueFieldUnaryManager() {
		return OneDev.getInstance(IssueFieldUnaryManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Issue issue = new Issue();
		issue.setProject(getProject());
		Serializable fieldBean = getIssueFieldUnaryManager().readFields(issue);
		
		if (queryString != null) {
			try {
				IssueQuery query = IssueQuery.parse(getProject(), queryString, true, true);
				if (query.getCriteria() != null)
					query.getCriteria().populate(issue, fieldBean);
			} catch (Exception e) {
			}
		} 
		issue.setSubmitDate(new Date());
		issue.setState(getProject().getIssueWorkflow().getInitialStateSpec().getName());
		issue.setSubmitter(getLoginUser());
		
		milestoneName = issue.getMilestoneName();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				issue.setProject(getProject());
				issue.setSubmitter(getLoginUser());
				issue.setMilestone(getProject().getMilestone(milestoneName));
				getIssueManager().open(issue, fieldBean);
				setResponsePage(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null));
			}
			
		};
		
		TextField<String> titleInput = new TextField<String>("title", new PropertyModel<String>(issue, "title")); 
		titleInput.setRequired(true).setLabel(Model.of("Title"));
		form.add(titleInput);
		form.add(new FencedFeedbackPanel("titleFeedback", titleInput));
		
		titleInput.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !titleInput.isValid()?" has-error":"";
			}
			
		}));
		
		form.add(new CommentInput("description", new PropertyModel<String>(issue, "description"), false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), issue.getUUID());
			}

			@Override
			protected Project getProject() {
				return NewIssuePage.this.getProject();
			}
			
		});

		List<String> milestones = getProject().getMilestones().stream().map(it->it.getName()).collect(Collectors.toList());
		StringSingleChoice choice = new StringSingleChoice("milestone", 
				new PropertyModel<String>(this, "milestoneName"), milestones) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("No milestone");
			}
			
		};
		choice.setRequired(false);
		form.add(choice);
		
		Set<String> excludedFields = getIssueFieldUnaryManager().getExcludedProperties(issue, 
				getProject().getIssueWorkflow().getInitialStateSpec().getName());
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
