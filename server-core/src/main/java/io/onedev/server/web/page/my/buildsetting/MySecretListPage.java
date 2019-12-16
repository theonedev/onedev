package io.onedev.server.web.page.my.buildsetting;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.JobSecret;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.web.editable.PropertyContext;

@SuppressWarnings("serial")
public class MySecretListPage extends MyBuildSettingPage {

	public MySecretListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Set<String> names = new HashSet<>();
				for (JobSecret secret: getLoginUser().getBuildSetting().getSecrets()) {
					if (names.contains(secret.getName())) {
						error("Duplicate name found: " + secret.getName());
						return;
					} else {
						names.add(secret.getName());
					}
				}
				
				OneDev.getInstance(UserManager.class).save(getLoginUser());
				getSession().success("Job secrets have been saved");
				setResponsePage(MySecretListPage.class);
			}
			
		};
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		form.add(PropertyContext.editModel("editor", new AbstractReadOnlyModel<Serializable>() {

			@Override
			public Serializable getObject() {
				return getLoginUser().getBuildSetting();
			}
			
		}, "secrets"));
		
		add(form);
		
		String note = String.format("Define commons secrets to be used in build jobs of all projects "
				+ "owned by me. Secret value less than %d characters will not be masked "
				+ "in build log", SecretInput.MASK.length());
		add(new Label("note", note).setEscapeModelStrings(false));
	}

}
