package io.onedev.server.plugin.pack.helm;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Pack;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;

public class HelmPackPanel extends GenericPanel<Pack> {
	
	public HelmPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + getPack().getProject().getPath() + "/~" + HelmPackHandler.HANDLER_ID;
		add(new Label("addRepo", "$ helm repo add onedev --username <onedev_account_name> --password <onedev_password_or_access_token> " + registryUrl));
		add(new Label("installChart", "$ helm install " + getPack().getName() + " onedev/" + getPack().getName() + " --version " + getPack().getVersion()));
		
		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return "" +
						"# " + _T("Use job token to tell OneDev the build using the package") + "\n" +
						"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package read permission") + "\n" +
						"helm install " + getPack().getName() + " --username @job_token@ --password @secret:access-token@ " + registryUrl + "/" + getPack().getName() + "-" +getPack().getVersion() + ".tgz";
			}
			
		}));

		add(new ListView<Map.Entry<String, Object>>("attributes", new LoadableDetachableModel<>() {
			@Override
			protected List<Map.Entry<String, Object>> load() {
				var metadata = ((HelmData) getPack().getData()).getMetadata();
				var metadataItems = new ArrayList<Map.Entry<String, Object>>();
				for (var entry : metadata.entrySet()) {
					if (!entry.getKey().equals("name") && !entry.getKey().equals("version")) {
						metadataItems.add(entry);
					}
				}				
				return metadataItems;
			}
		}) {
			@Override
			protected void populateItem(ListItem<Map.Entry<String, Object>> item) {
				var entry = item.getModelObject();
				item.add(new Label("name", entry.getKey()));
				item.add(new Label("value", String.valueOf(entry.getValue())));
			}
		});
	}

	private Pack getPack() {
		return getModelObject();
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
} 