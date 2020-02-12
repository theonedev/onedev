package io.onedev.server.web.page.project.setting.build;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.web.component.build.secret.JobSecretListPanel;
import io.onedev.server.web.component.build.secret.JobSecretsBean;
import io.onedev.server.web.component.link.SettingInOwnerLink;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.user.buildsetting.UserJobSecretsPage;
import io.onedev.server.web.page.my.buildsetting.MyJobSecretsPage;

@SuppressWarnings("serial")
public class ProjectJobSecretsPage extends ProjectBuildSettingPage {

	public ProjectJobSecretsPage(PageParameters params) {
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
		add(new JobSecretListPanel("projectSpecificJobSecrets", bean) {
			
			@Override
			protected void onSaved(List<JobSecret> secrets) {
				getProject().getBuildSetting().setJobSecrets(secrets);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(ProjectJobSecretsPage.class, ProjectJobSecretsPage.paramsOf(getProject()));
			}
			
		});
		
		List<JobSecret> inheritedSecrets = getProject().getBuildSetting().getInheritedSecrets(getProject());
		bean = new JobSecretsBean();
		bean.setSecrets(inheritedSecrets);
		add(PropertyContext.view("inheritedJobSecrets", bean, "secrets"));
		
		add(new SettingInOwnerLink("owner", projectModel, UserJobSecretsPage.class, MyJobSecretsPage.class));
	}

}
