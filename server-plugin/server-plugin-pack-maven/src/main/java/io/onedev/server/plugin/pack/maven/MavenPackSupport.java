package io.onedev.server.plugin.pack.maven;

import static io.onedev.server.plugin.pack.maven.MavenPackHandler.NONE;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.io.Resources;

import io.onedev.server.OneDev;
import io.onedev.server.service.PackService;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;

public class MavenPackSupport implements PackSupport {

	public static final String TYPE = "Maven";
	
	@Override
	public int getOrder() {
		return 200;
	}

	@Override
	public String getPackType() {
		return TYPE;
	}

	@Override
	public String getPackIcon() {
		return "maven";
	}

	@Override
	public String getReference(Pack pack, boolean withProject) {
		String reference;
		if (!pack.getName().endsWith(NONE) && !pack.getVersion().equals(NONE))
			reference = pack.getName() + ":" + pack.getVersion();
		else 
			reference = substringBeforeLast(pack.getName(), ":") + ":<Plugins Metadata>";
		
		if (withProject)
			reference = pack.getProject().getPath() + "/" + reference;
		return reference;
	}

	@Override
	public Component renderContent(String componentId, Pack pack) {
		var packId = pack.getId();
		return new MavenPackPanel(componentId, new LoadableDetachableModel<>() {
			@Override
			protected Pack load() {
				return OneDev.getInstance(PackService.class).load(packId);
			}
			
		});
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new MavenHelpPanel(componentId, project.getPath());
	}

	public static String getJobCommandsTemplate() throws IOException {
		var tplUrl = Resources.getResource(MavenPackSupport.class, "job-commands.tpl");
		var template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
		template = template.replace(
			"maven:job-token-notice", 
			_T("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages"));
		template = template.replace(
			"maven:access-token-notice", 
			_T("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission"));
		template = template.replace(
			"maven:allow-http-notice", 
			_T("Add below to allow accessing via http protocol in new Maven versions"));
		return template;
	}

	public static String getServersAndMirrorsTemplate() throws IOException {
		var tplUrl = Resources.getResource(MavenPackSupport.class, "servers-and-mirrors.tpl");
		var template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
		template = template.replace(
			"maven:has-permission-notice", 
			_T("Make sure the account has package ${permission} permission over the project"));
		template = template.replace(
			"maven:allow-http-notice", 
			_T("Add below to allow accessing via http protocol in new Maven versions"));
		return template;
	}
}
