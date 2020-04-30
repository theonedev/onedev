package io.onedev.server.web.page.project.setting.build;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class JobSecretsPage extends BuildSettingPage {

	public JobSecretsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String note = String.format("Define job secrets to be used in build spec. Secret value less "
				+ "than %d characters will not be masked in build log", SecretInput.MASK.length());
		add(new Label("note", note).setEscapeModelStrings(false));
		
		JobSecretsBean bean = new JobSecretsBean();
		bean.setSecrets(getProject().getBuildSetting().getJobSecrets());
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "secrets");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Set<String> names = new HashSet<>();
				for (JobSecret secret: bean.getSecrets()) {
					if (names.contains(secret.getName())) {
						error("Duplicate name found: " + secret.getName());
						return;
					} else {
						names.add(secret.getName());
					}
				}
				getSession().success("Job secrets saved");
				getProject().getBuildSetting().setJobSecrets(bean.getSecrets());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(JobSecretsPage.class, JobSecretsPage.paramsOf(getProject()));
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		add(form);
		
	}

}
