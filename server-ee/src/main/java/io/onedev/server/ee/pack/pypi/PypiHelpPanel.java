package io.onedev.server.ee.pack.pypi;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class PypiHelpPanel extends Panel {
	
	private final String projectPath;
	
	public PypiHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + PypiPackService.SERVICE_ID;
		add(new CodeSnippetPanel("addRepository", new AbstractReadOnlyModel<>() {
			@Override
			public String getObject() {
				return "" +
						"[distutils]\n" + 
						"index-servers=\n" +
						"  onedev\n\n" +
						"[onedev]\n" + 
						"repository=" + registryUrl + "\n" + 
						"username={username}\n" +
						"password={password_or_access_token}";
			}
			
		}));

		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<>() {
			@Override
			protected String load() {
				return "" +
						"# Use job token to tell OneDev the build publishing the package\n" +
						"# Job secret 'access-token' should be defined in project build setting as an access token with package write permission\n\n" +
						"cat << EOF > $HOME/.pypirc\n" +
						"[distutils]\n" +
						"index-servers=\n" +
						"  onedev\n\n" +
						"[onedev]\n" +
						"repository=" + registryUrl + "\n" +
						"username=@job_token@\n" +
						"password=@secret:access-token@\n" +
						"EOF";
			}

		}));
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
	}
	
}
