package io.onedev.server.plugin.pack.cargo;

import static io.onedev.server.web.translation.Translation._T;

import java.io.IOException;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.model.Pack;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadPack;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;

public class CargoPackPanel extends GenericPanel<Pack> {

	public CargoPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + getPack().getProject().getPath() + "/~" + CargoPackHandler.HANDLER_ID + "/";
		var canAccessAnonymously = SecurityUtils.asAnonymous().isPermitted(
				new ProjectPermission(getPack().getProject(), new ReadPack()));
		add(new CodeSnippetPanel("registryConfig", Model.of("" +
				"[registries.onedev]\n" +
				"index = \"sparse+" + registryUrl + "\"\n" +
				"credential-provider = \"cargo:token\"")));
		var registryAuth = new CodeSnippetPanel("registryAuth",
				Model.of("$ cargo login --registry onedev <onedev_access_token>"));
		registryAuth.setVisible(!canAccessAnonymously);
		add(registryAuth);
		add(new WebMarkupContainer("readPermissionNote").setVisible(!canAccessAnonymously));
		add(new CodeSnippetPanel("install", Model.of(
				"$ cargo add " + getPack().getName() + "@" + getPack().getVersion() + " --registry onedev")));

		var jobCommands = "" +
				"# " + _T("Use job token to tell OneDev the build using the package") + "\n" +
				"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package read permission") + "\n\n" +
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
		add(new CodeSnippetPanel("jobCommands", Model.of(jobCommands)));

		var attributesView = new RepeatingView("attributes");
		try {
			var metadata = OneDev.getInstance(ObjectMapper.class).readTree(((CargoData) getPack().getData()).getMetadata());
			addAttribute(attributesView, "description", metadata.path("description").asText(""));
			addAttribute(attributesView, "repository", metadata.path("repository").asText(""));
			addAttribute(attributesView, "documentation", metadata.path("documentation").asText(""));
			addAttribute(attributesView, "homepage", metadata.path("homepage").asText(""));
			addAttribute(attributesView, "license", metadata.path("license").asText(""));
			addAttribute(attributesView, "rust_version", metadata.path("rust_version").asText(""));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		add(attributesView);
	}

	private void addAttribute(RepeatingView attributesView, String name, String value) {
		if (value.length() != 0) {
			var item = new WebMarkupContainer(attributesView.newChildId());
			item.add(new Label("name", name));
			item.add(new MultilineLabel("value", value));
			attributesView.add(item);
		}
	}

	private Pack getPack() {
		return getModelObject();
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
}
