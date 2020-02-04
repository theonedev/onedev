package io.onedev.server.web.page.my.buildsetting;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.web.component.build.secret.JobSecretListPanel;
import io.onedev.server.web.component.build.secret.JobSecretsBean;

@SuppressWarnings("serial")
public class MyJobSecretsPage extends MyBuildSettingPage {

	public MyJobSecretsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobSecretsBean bean = new JobSecretsBean();
		bean.setSecrets(getBuildSetting().getJobSecrets());
		add(new JobSecretListPanel("jobSecrets", bean) {

			@Override
			protected void onSaved(List<JobSecret> secrets) {
				getBuildSetting().setJobSecrets(secrets);
				OneDev.getInstance(UserManager.class).save(getLoginUser());
				setResponsePage(MyJobSecretsPage.class);
			}
			
		});
		
		String note = String.format("Define common job secrets to be used in all projects "
				+ "owned by me. Secret value less than %d characters will not be masked "
				+ "in build log", SecretInput.MASK.length());
		add(new Label("note", note).setEscapeModelStrings(false));
	}

}
