package io.onedev.server.plugin.pack.maven;

import static io.onedev.server.util.GroovyUtils.evalTemplate;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.google.common.io.Resources;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;

public class MavenHelpPanel extends Panel {
	
	private final String projectPath;
	
	public MavenHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
		var bindings = new HashMap<String, Object>();
		bindings.put("url", serverUrl + "/" + projectPath + "/~" + MavenPackHandler.HANDLER_ID);
		bindings.put("permission", "write");

		try {
			URL tplUrl = Resources.getResource(MavenHelpPanel.class, "repositories.tpl");
			String template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
			add(new CodeSnippetPanel("pom", Model.of(evalTemplate(template, bindings))));
			add(new CodeSnippetPanel("settings", Model.of(evalTemplate(MavenPackSupport.getServersAndMirrorsTemplate(), bindings))));
			add(new CodeSnippetPanel("jobCommands", Model.of(evalTemplate(MavenPackSupport.getJobCommandsTemplate(), bindings))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
