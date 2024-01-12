package io.onedev.server.ee.pack.npm;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import static org.apache.commons.lang3.StringUtils.substringAfter;

public class NpmHelpPanel extends Panel {
	
	private final String projectPath;
	
	public NpmHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + NpmPackService.SERVICE_ID + "/";
		add(new Label("scopeRegistry", "$ npm config set @myscope:registry " + registryUrl));
		add(new Label("registryAuth", "$ npm config set -- '" + substringAfter(registryUrl, ":") + ":_authToken' \"onedev_access_token\""));

		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<>() {

			@Override
			protected String load() {
				var registryUrl = getServerUrl() + "/" + projectPath + "/~npm/";
				return "" +
						"# Use @@ to reference scope in job commands to avoid being interpreted as variable\n" +
						"npm config set @@myscope:registry " + registryUrl + "\n\n" +
						"# Use job token to tell OneDev the build publishing the package\n" +
						"# Job secret 'access-token' should be defined in project build setting as an access token with package write permission\n" +
						"npm config set -- '" + substringAfter(registryUrl, ":") + ":_authToken' \"@job_token@:@secret:access-token@\"\n\n" +
						"npm publish";
			}

		}));
		
	}
	
	private String getServerUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
	}
	
}
