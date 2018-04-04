package io.onedev.server.web.page.project.issues.newissue;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class NewIssuePage extends ProjectPage implements InputContext {

	public NewIssuePage(PageParameters params) {
		super(params);
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Issue issue = new Issue();
		issue.setProject(getProject());
		
		Form<?> form = new Form<Void>("form");
		form.add(BeanContext.editBean("generalSetting", issue));
		
		Serializable fieldsBean;
		try {
			fieldsBean = getIssueManager().defineFieldBeanClass(getProject()).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		form.add(BeanContext.editBean("fields", fieldsBean));
		
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

}
