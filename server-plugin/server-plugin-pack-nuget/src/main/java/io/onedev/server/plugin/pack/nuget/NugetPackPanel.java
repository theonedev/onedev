package io.onedev.server.plugin.pack.nuget;

import static io.onedev.server.web.translation.Translation._T;

import java.nio.charset.StandardCharsets;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.model.Pack;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadPack;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;

public class NugetPackPanel extends GenericPanel<Pack> {
	
	public NugetPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + getPack().getProject().getPath() + "/~" + NugetPackHandler.HANDLER_ID + "/index.json";
		var canAccessAnonymously = SecurityUtils.asAnonymous().isPermitted(
				new ProjectPermission(getPack().getProject(), new ReadPack()));
		var authPart = canAccessAnonymously ? "" : "--username <onedev_account_name> --password <onedev_password_or_access_token> ";
		add(new Label("addSource", "$ dotnet nuget add source --name onedev " + authPart + "--store-password-in-clear-text " + registryUrl));
		add(new WebMarkupContainer("readPermissionNote").setVisible(!canAccessAnonymously));
		add(new Label("addPack", "$ dotnet add package " + getPack().getName() + " -v " + getPack().getVersion()));
		
		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return "" +
						"# " + _T("Use job token to tell OneDev the build using the package") + "\n" +
						"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package read permission") + "\n" +
						"dotnet nuget add source --name onedev --username @job_token@ --password @secret:access-token@ --store-password-in-clear-text " + registryUrl;
			}
			
		}));
		add(new CodeSnippetPanel("nuspec", new LoadableDetachableModel<>() {
			@Override
			protected String load() {
				var data = (NugetData) getPack().getData();
				return new String(data.getMetadata(), StandardCharsets.UTF_8);
			}
			
		}));
	}

	private Pack getPack() {
		return getModelObject();
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
}
