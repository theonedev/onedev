package io.onedev.server.plugin.pack.maven;

import static io.onedev.server.util.GroovyUtils.evalTemplate;
import static io.onedev.server.web.translation.Translation._T;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.google.common.io.Resources;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;

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
		bindings.put("canAccessAnonymously", false);

		List<Tab> buildToolTabs = new ArrayList<>();
		buildToolTabs.add(new AjaxActionTab(Model.of(_T("Maven"))) {
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component content = newMavenInstructions(bindings);
				target.add(content);
				MavenHelpPanel.this.replace(content);
			}
		}.setSelected(true));
		buildToolTabs.add(new AjaxActionTab(Model.of(_T("Gradle"))) {
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component content = newGradleInstructions(bindings);
				target.add(content);
				MavenHelpPanel.this.replace(content);
			}
		});
		add(new Tabbable("buildToolTabs", buildToolTabs));

		add(newMavenInstructions(bindings));
	}

	private Component newMavenInstructions(Map<String, Object> bindings) {
		var fragment = new Fragment("buildToolInstructions", "mavenInstructionsFrag", this);
		try {
			URL tplUrl = Resources.getResource(MavenHelpPanel.class, "repositories.tpl");
			String template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
			fragment.add(new CodeSnippetPanel("pom", Model.of(evalTemplate(template, bindings).trim())));
			fragment.add(new CodeSnippetPanel("settings", Model.of(evalTemplate(MavenPackSupport.getServersAndMirrorsTemplate(), bindings).trim())));
			fragment.add(new CodeSnippetPanel("jobCommands", Model.of(evalTemplate(MavenPackSupport.getJobCommandsTemplate(), bindings).trim())));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		fragment.setOutputMarkupId(true);
		return fragment;
	}

	private Component newGradleInstructions(Map<String, Object> bindings) {
		var fragment = new Fragment("buildToolInstructions", "gradleInstructionsFrag", this);
		try {
			var groovyTpl = MavenPackSupport.getGradleDependencyTemplate("gradle-publish-groovy.tpl");
			var kotlinTpl = MavenPackSupport.getGradleDependencyTemplate("gradle-publish-kotlin.tpl");
			fragment.add(new CodeSnippetPanel("gradleGroovy", Model.of(evalTemplate(groovyTpl, bindings).trim())));
			fragment.add(new CodeSnippetPanel("gradleKotlin", Model.of(evalTemplate(kotlinTpl, bindings).trim())));
			fragment.add(new CodeSnippetPanel("gradleProperties", Model.of(evalTemplate(MavenPackSupport.getGradlePropertiesTemplate(), bindings).trim())));
			fragment.add(new CodeSnippetPanel("gradleJobCommands", Model.of(evalTemplate(MavenPackSupport.getGradleJobCommandsTemplate(), bindings).trim())));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		fragment.setOutputMarkupId(true);
		return fragment;
	}

}
