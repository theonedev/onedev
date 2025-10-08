package io.onedev.server.plugin.pack.npm;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import static io.onedev.server.web.translation.Translation._T;
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

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + NpmPackHandler.HANDLER_ID + "/";
		add(new CodeSnippetPanel("scopeRegistry", Model.of("$ npm config set @myscope:registry " + registryUrl)));
		add(new CodeSnippetPanel("registryAuth", Model.of("$ npm config set -- '" + substringAfter(registryUrl, ":") + ":_authToken' \"onedev_access_token\"")));
		add(new CodeSnippetPanel("publishCommand", Model.of("$ npm publish")));
		
		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<>() {

			@Override
			protected String load() {
				var registryUrl = getServerUrl() + "/" + projectPath + "/~npm/";
				return "" +
						"# " + _T("Use @@ to reference scope in job commands to avoid being interpreted as variable") + "\n\n" +
						"npm config set @@myscope:registry " + registryUrl + "\n\n" +
						"# " + _T("Use job token to tell OneDev the build publishing the package") + "\n" +
						"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package write permission") + "\n\n" +
						"npm config set -- '" + substringAfter(registryUrl, ":") + ":_authToken' \"@job_token@:@secret:access-token@\"\n\n" +
						"npm publish";
			}

		}));
		
	}
	
	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
}
