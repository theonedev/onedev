package io.onedev.server.ee.pack.nuget;

import io.onedev.server.OneDev;
import io.onedev.server.ee.pack.npm.NpmPackService;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;

import static org.apache.commons.lang3.StringUtils.substringAfter;

public class NugetHelpPanel extends Panel {
	
	private final String projectPath;
	
	public NugetHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + NugetPackService.SERVICE_ID + "/index.json";
		add(new Label("addSource", "$ dotnet nuget add source --name onedev --username <onedev_account_name> --password <onedev_password_or_access_token> --store-password-in-clear-text " + registryUrl));

		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return "" +
						"# Use job token to tell OneDev the build pushing the package\n" +
						"# Job secret 'access-token' should be defined in project build setting as an access token with package write permission\n" +
						"dotnet nuget add source --name onedev --username @job_token@ --password @secret:access-token@ --store-password-in-clear-text " + registryUrl;
			}

		}));
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
	}
	
}
