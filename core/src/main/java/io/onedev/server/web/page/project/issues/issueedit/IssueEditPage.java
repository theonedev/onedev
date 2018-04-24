package io.onedev.server.web.page.project.issues.issueedit;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;

@SuppressWarnings("serial")
public class IssueEditPage extends ProjectPage implements InputContext {

	public static final String PARAM_ISSUE = "issue";
	
	private final IModel<Issue> issueModel;
	
	public IssueEditPage(PageParameters params) {
		super(params);
		
		issueModel = new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				return getIssueManager().load(params.get(PARAM_ISSUE).toLong());
			}

		};
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private IssueFieldManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldManager.class);
	}
	
	private Issue getIssue() {
		return issueModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		
		Issue issue = getIssue();
		form.add(BeanContext.editBean("builtin", issue));

		Serializable fieldBean = getIssueFieldManager().readFields(issue); 
		
		Map<String, PropertyDescriptor> propertyDescriptors = 
				new BeanDescriptor(fieldBean.getClass()).getMapOfDisplayNameToPropertyDescriptor();
		
		Set<String> excludedFields = new HashSet<>();
		for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFields()) {
			if (!issue.getMultiValueFields().containsKey(fieldSpec.getName()))
				excludedFields.add(propertyDescriptors.get(fieldSpec.getName()).getPropertyName());
		}

		form.add(BeanContext.editBean("fields", fieldBean, excludedFields));
		
		form.add(new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				new BeanDescriptor(Issue.class).copyProperties(issue, getIssue());
				getIssueManager().save(getIssue(), fieldBean, issue.getMultiValueFields().keySet());
				
				setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
			}
			
		});
		
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
			}
			
		});
		add(form);
	}

	public static PageParameters paramsOf(Issue issue) {
		PageParameters params = ProjectPage.paramsOf(issue.getProject());
		params.set(PARAM_ISSUE, issue.getId());
		return params;
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
