package io.onedev.server.web.page.project.setting.issue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;

@SuppressWarnings("serial")
public class PromptFieldsUponIssueOpenSettingPage extends IssueSettingPage {

	private Set<String> fieldSet;
	
	public PromptFieldsUponIssueOpenSettingPage(PageParameters params) {
		super(params);
		fieldSet = getSetting().getPromptFieldsUponIssueOpen(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSetting().setPromptFieldsUponIssueOpen(fieldSet);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(PromptFieldsUponIssueOpenSettingPage.class, PromptFieldsUponIssueOpenSettingPage.paramsOf(getProject()));
				Session.get().success("Setting saved");
			}
			
		};
		
		form.add(new StringMultiChoice("fields", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				return fieldSet;
			}

			@Override
			public void setObject(Collection<String> object) {
				fieldSet = new HashSet<>(object);
			}
			
		}, getGlobalSetting().getFieldNames()));
		
		form.add(new Link<Void>("useDefault") {

			@Override
			public void onClick() {
				getSetting().setPromptFieldsUponIssueOpen(null);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(PromptFieldsUponIssueOpenSettingPage.class, PromptFieldsUponIssueOpenSettingPage.paramsOf(getProject()));
				Session.get().success("Reset to default setting");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getSetting().getPromptFieldsUponIssueOpen(false) != null);
			}
			
		});
		add(form);
		
		form.setOutputMarkupId(true);
	}

}
