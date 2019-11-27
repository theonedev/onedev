package io.onedev.server.web.page.project.setting.secret;

import java.io.Serializable;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class SecretListPage extends ProjectSettingPage {

	public SecretListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(ProjectManager.class).save(getProject());
				getSession().success("Project secrets have been saved");
				setResponsePage(SecretListPage.class, SecretListPage.paramsOf(getProject()));
			}
			
		};
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		form.add(PropertyContext.editModel("editor", new AbstractReadOnlyModel<Serializable>() {

			@Override
			public Serializable getObject() {
				return getProject();
			}
			
		}, "secrets"));
		
		add(form);
		
		String note = String.format("Define project secrets here to be used in build jobs. Secret value less "
				+ "than %d characters will not be masked in build log. For a given name, the first "
				+ "authorized secret will be used", SecretInput.MASK.length());
		add(new Label("note", note).setEscapeModelStrings(false));
	}

}
