package io.onedev.server.plugin.pack.cargo;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;

public class CargoHelpPanel extends Panel {

	private final String projectPath;

	public CargoHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + CargoPackHandler.HANDLER_ID + "/";
		add(new CodeSnippetPanel("addRegistry", Model.of("" +
				"[registries.onedev]\n" +
				"index = \"sparse+" + registryUrl + "\"\n" +
				"credential-provider = \"cargo:token\"")));

		add(new CodeSnippetPanel("registryAuth", Model.of("$ cargo login --registry onedev <onedev_access_token>")));

		add(new CodeSnippetPanel("publishCommand", Model.of("$ cargo publish --registry onedev")));

		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<>() {
			@Override
			protected String load() {
				return "" +
						"# " + _T("Use job token to tell OneDev the build publishing the package") + "\n" +
						"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package write permission") + "\n\n" +
						"mkdir -p $HOME/.cargo\n" +
						"cat << EOF >> $HOME/.cargo/config.toml\n" +
						"[registries.onedev]\n" +
						"index = \"sparse+" + registryUrl + "\"\n" +
						"credential-provider = \"cargo:token\"\n" +
						"EOF\n\n" +
						"cat << EOF >> $HOME/.cargo/credentials.toml\n" +
						"[registries.onedev]\n" +
						"token = \"@job_token@:@secret:access-token@\"\n" +
						"EOF";
			}
		}));
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
}
