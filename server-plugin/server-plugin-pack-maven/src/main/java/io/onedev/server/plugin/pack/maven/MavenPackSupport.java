package io.onedev.server.plugin.pack.maven;

import static io.onedev.server.plugin.pack.maven.MavenPackHandler.NONE;
import static io.onedev.server.util.GroovyUtils.evalTemplate;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Pattern PERMISSION_CHOICE = Pattern.compile(
			"\\$\\{permission\\.equals\\(\"write\"\\)\\s*\\?\\s*\"([^\"]*)\"\\s*:\\s*\"([^\"]*)\"\\s*}");
	
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

	public static String getJobCommands(Map<String, Object> bindings) throws IOException {
		var tplUrl = Resources.getResource(MavenPackSupport.class, "job-commands.tpl");
		var commands = evalTemplate(Resources.toString(tplUrl, StandardCharsets.UTF_8), bindings);
		commands = commands.replace(
			"maven:job-token-notice", 
			getJobTokenNotice(bindings));
		commands = commands.replace(
			"maven:access-token-notice", 
			_T("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission")
				.replace("${permission}", bindings.get("permission").toString()));
		commands = commands.replace(
			"maven:allow-http-notice", 
			_T("Add below to allow accessing via http protocol in new Maven versions"));
		return commands;
	}

	private static String getJobTokenNotice(Map<String, Object> bindings) {
		// Insert translated branding only after rendering the template so that quotes,
		// dollar signs and backslashes in the product name remain literal text.
		var notice = _T("Use job token as user name so that ${productName} can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages");
		var choice = "write".equals(bindings.get("permission")) ? 1 : 2;
		return PERMISSION_CHOICE.matcher(notice).replaceAll(match -> Matcher.quoteReplacement(match.group(choice)));
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

	public static String getGradleDependencyTemplate(String templateName) throws IOException {
		var tplUrl = Resources.getResource(MavenPackSupport.class, templateName);
		var template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
		template = template.replace(
			"gradle:has-permission-notice",
			_T("Make sure the account has package ${permission} permission over the project"));
		template = template.replace(
			"gradle:allow-http-notice",
			_T("Add below to allow accessing via http protocol"));
		return template;
	}

	public static String getGradlePropertiesTemplate() throws IOException {
		var tplUrl = Resources.getResource(MavenPackSupport.class, "gradle-properties.tpl");
		var template = Resources.toString(tplUrl, StandardCharsets.UTF_8);
		return template.replace(
			"gradle:has-permission-notice",
			_T("Make sure the account has package ${permission} permission over the project"));
	}

	public static String getGradleJobCommands(Map<String, Object> bindings) throws IOException {
		var tplUrl = Resources.getResource(MavenPackSupport.class, "gradle-job-commands.tpl");
		var commands = evalTemplate(Resources.toString(tplUrl, StandardCharsets.UTF_8), bindings);
		commands = commands.replace(
			"gradle:job-token-notice",
			getJobTokenNotice(bindings));
		commands = commands.replace(
			"gradle:access-token-notice",
			_T("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission")
				.replace("${permission}", bindings.get("permission").toString()));
		return commands;
	}
}
