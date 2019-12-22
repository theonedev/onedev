package io.onedev.server.web.page.project.setting.build;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.JobSecret;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.web.component.job.secret.JobSecretListPanel;
import io.onedev.server.web.component.job.secret.JobSecretsBean;
import io.onedev.server.web.component.link.SettingInOwnerLink;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.user.buildsetting.UserSecretsPage;
import io.onedev.server.web.page.my.buildsetting.MySecretsPage;

@SuppressWarnings("serial")
public class ProjectSecretsPage extends ProjectBuildSettingPage {

	public ProjectSecretsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String note = String.format("Define secrets to be used in build jobs. Secret value less "
				+ "than %d characters will not be masked in build log", SecretInput.MASK.length());
		add(new Label("note", note).setEscapeModelStrings(false));
		
		JobSecretsBean bean = new JobSecretsBean();
		bean.setSecrets(getProject().getBuildSetting().getSecrets());
		add(new JobSecretListPanel("projectSpecificSecrets", bean) {
			
			@Override
			protected void onSaved(List<JobSecret> secrets) {
				getProject().getBuildSetting().setSecrets(secrets);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(ProjectSecretsPage.class, ProjectSecretsPage.paramsOf(getProject()));
			}
			
		});
		
		List<JobSecret> inheritedSecrets = getProject().getBuildSetting().getInheritedSecrets(getProject());
		bean = new JobSecretsBean();
		bean.setSecrets(inheritedSecrets);
		add(PropertyContext.view("inheritedSecrets", bean, "secrets"));
		
		add(new SettingInOwnerLink("owner", projectModel, UserSecretsPage.class, MySecretsPage.class));
	}

}
