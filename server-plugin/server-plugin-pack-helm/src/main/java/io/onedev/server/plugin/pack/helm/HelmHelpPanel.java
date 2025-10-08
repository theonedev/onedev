package io.onedev.server.plugin.pack.helm;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class HelmHelpPanel extends Panel {
	
	private final String projectPath;
	
	public HelmHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + HelmPackHandler.HANDLER_ID;
		add(new CodeSnippetPanel("pushChart", Model.of("$ curl -u <onedev_account_name>:<onedev_password_or_access_token> -X POST --upload-file /path/to/chart.tgz " + registryUrl)));
		
		add(new CodeSnippetPanel("jobCommands", Model.of("" +
				"# " + _T("Use job token to tell OneDev the build pushing the chart") + "\n" +
				"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package write permission") + "\n\n" +
				"curl -u @job_token@:@secret:access-token@ -X POST --upload-file /path/to/chart.tgz " + registryUrl)));
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
} 