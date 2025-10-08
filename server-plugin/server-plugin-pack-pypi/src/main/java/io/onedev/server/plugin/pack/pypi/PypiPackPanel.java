package io.onedev.server.plugin.pack.pypi;

import com.google.common.base.Joiner;
import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Pack;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import static io.onedev.server.web.translation.Translation._T;

import java.net.MalformedURLException;
import java.net.URL;

public class PypiPackPanel extends GenericPanel<Pack> {
	
	public PypiPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var serverUrl = getServerUrl();
		String protocol;
		String host;
		try {
			var url = new URL(serverUrl);
			 protocol = url.getProtocol();
			 host = url.getHost();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		var indexUrl = protocol + "://<OneDev_account_name>:<OneDev_password>@" + UrlUtils.getServer(serverUrl) 
				+ "/" + getPack().getProject().getPath() + "/~" + PypiPackHandler.HANDLER_ID + "/simple/";
		var installCmd = "$ python3 -m pip install --extra-index-url " + indexUrl;
		if (protocol.equals("http"))
			installCmd += " --trusted-host " + host;
		installCmd += " " + getPack().getName() + "==" + getPack().getVersion();
		add(new Label("install", installCmd));

		indexUrl = protocol + "://@job_token@:@secret:access-token@@@" + UrlUtils.getServer(serverUrl)
				+ "/" + getPack().getProject().getPath() + "/~" + PypiPackHandler.HANDLER_ID + "/simple/";
		var jobCommands = "" +
				"# " + _T("Use job token to tell OneDev the build using the package") + "\n" +
				"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package read permission") + "\n\n" +
				"python3 -m pip install --extra-index-url " + indexUrl;
		if (protocol.equals("http"))		
			jobCommands += " --trusted-host " + host;
		jobCommands += " -r requirements.txt";
		
		add(new CodeSnippetPanel("jobCommands", Model.of(jobCommands)));
		
		var attributes = ((PypiData) getPack().getData()).getAttributes();
		
		var description = attributes.get("description");
		
		if (description != null) {
			var descriptionContentType = attributes.get("description_content_type");
			if (descriptionContentType != null && descriptionContentType.get(0).contains("markdown")) 
				add(new MarkdownViewer("description", Model.of(description.get(0)), null));				
			else 
				add(new MultilineLabel("description", description.get(0)));
		} else {
			add(new WebMarkupContainer("description").setVisible(false));
		}
	
		var attributesView = new RepeatingView("attributes");
		for (var entry: attributes.entrySet()) {
			if (!entry.getKey().equals("description_content_type")
					&& !entry.getKey().equals("description")) {
				var item = new WebMarkupContainer(attributesView.newChildId());
				item.add(new Label("name", entry.getKey()));
				item.add(new MultilineLabel("value", Joiner.on("\n").join(entry.getValue())));
				attributesView.add(item);
			}
		}
		add(attributesView);
	}

	private Pack getPack() {
		return getModelObject();
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
}
