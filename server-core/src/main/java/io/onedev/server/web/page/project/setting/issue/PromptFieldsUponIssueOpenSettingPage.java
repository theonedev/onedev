package io.onedev.server.web.page.project.setting.issue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;

@SuppressWarnings("serial")
public class PromptFieldsUponIssueOpenSettingPage extends ProjectIssueSettingPage {

	private Collection<String> fieldSet;
	
	public PromptFieldsUponIssueOpenSettingPage(PageParameters params) {
		super(params);
		fieldSet = getProjectSetting().getPromptFieldsUponIssueOpen(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getProjectSetting().setPromptFieldsUponIssueOpen(fieldSet);
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
			
		}, new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> choices = new LinkedHashMap<>();
				for (String fieldName: getSetting().getFieldNames())
					choices.put(fieldName, fieldName);
				return choices;
			}
			
		}));
		
		form.add(new Link<Void>("useDefault") {

			@Override
			public void onClick() {
				getProjectSetting().setPromptFieldsUponIssueOpen(null);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(PromptFieldsUponIssueOpenSettingPage.class, PromptFieldsUponIssueOpenSettingPage.paramsOf(getProject()));
				Session.get().success("Reset to default setting");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProjectSetting().getPromptFieldsUponIssueOpen(false) != null);
			}
			
		});
		add(form);
		
		form.setOutputMarkupId(true);
	}

}
