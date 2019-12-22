package io.onedev.server.web.page.admin.user.buildsetting;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.JobSecret;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.web.component.job.secret.JobSecretListPanel;
import io.onedev.server.web.component.job.secret.JobSecretsBean;

@SuppressWarnings("serial")
public class UserSecretsPage extends UserBuildSettingPage {

	public UserSecretsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobSecretsBean bean = new JobSecretsBean();
		bean.setSecrets(getBuildSetting().getSecrets());
		add(new JobSecretListPanel("secrets", bean) {

			@Override
			protected void onSaved(List<JobSecret> secrets) {
				getBuildSetting().setSecrets(secrets);
				OneDev.getInstance(UserManager.class).save(getUser());
				setResponsePage(UserSecretsPage.class, UserSecretsPage.paramsOf(getUser()));
			}
			
		});
		
		String note = String.format("Define common secrets to be used in build jobs of all projects "
				+ "owned by current user. Secret value less than %d characters will not be masked "
				+ "in build log", SecretInput.MASK.length());
		add(new Label("note", note).setEscapeModelStrings(false));
	}

}
