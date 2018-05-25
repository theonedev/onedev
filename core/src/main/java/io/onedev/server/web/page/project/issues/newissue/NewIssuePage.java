package io.onedev.server.web.page.project.issues.newissue;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.markdown.AttachmentSupport;
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
	
	private IssueFieldUnaryManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldUnaryManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Issue issue = new Issue();
		issue.setSubmitDate(new Date());
		issue.setState(getProject().getIssueWorkflow().getInitialStateSpec().getName());
		issue.setProject(getProject());
		issue.setSubmitter(getLoginUser());
		Serializable fieldBean = getIssueFieldManager().readFields(issue);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				issue.setProject(getProject());
				issue.setSubmitter(getLoginUser());
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
		
		Set<String> excludedFields = getIssueFieldManager().getExcludedFields(issue, 
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

}
