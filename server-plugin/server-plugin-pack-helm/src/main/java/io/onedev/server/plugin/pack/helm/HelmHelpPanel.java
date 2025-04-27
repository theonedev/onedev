package io.onedev.server.plugin.pack.helm;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
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

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + HelmPackService.SERVICE_ID;
		add(new CodeSnippetPanel("pushChart", Model.of("$ curl -u <onedev_account_name>:<onedev_password_or_access_token> -X POST --upload-file /path/to/chart.tgz " + registryUrl)));
		
		add(new CodeSnippetPanel("jobCommands", Model.of("" +
				"# Use job token to tell OneDev the build pushing the chart\n" +
				"# Job secret 'access-token' should be defined in project build setting as an access token with package write permission\n\n" +
				"curl -u @job_token@:@secret:access-token@ -X POST --upload-file /path/to/chart.tgz " + registryUrl)));
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
	}
} 