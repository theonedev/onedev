package io.onedev.server.ee.pack.maven;

import com.google.common.io.Resources;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static io.onedev.server.util.GroovyUtils.evalTemplate;

public class MavenHelpPanel extends Panel {
	
	private final String projectPath;
	
	public MavenHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		var bindings = new HashMap<String, Object>();
		bindings.put("url", serverUrl + "/" + projectPath + "/~maven");
		bindings.put("permission", "write");

		try {
			URL tplUrl = Resources.getResource(MavenHelpPanel.class, "repositories.tpl");
			String template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
			add(new CodeSnippetPanel("pom", Model.of(evalTemplate(template, bindings))));

			tplUrl = Resources.getResource(MavenHelpPanel.class, "servers-and-mirrors.tpl");
			template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
			add(new CodeSnippetPanel("settings", Model.of(evalTemplate(template, bindings))));

			tplUrl = Resources.getResource(MavenHelpPanel.class, "job-commands.tpl");
			template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
			add(new CodeSnippetPanel("jobCommands", Model.of(evalTemplate(template, bindings))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
