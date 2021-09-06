package io.onedev.server.web.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.resource.AbstractResource;

import io.onedev.agent.Agent;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.AgentToken;
import io.onedev.server.security.SecurityUtils;

public class AgentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		if (!new File(Bootstrap.installDir, "agent").exists())
			throw new ExplicitException("No agent package to download");

		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		response.setFileName("agent.zip");
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				File tempDir = FileUtils.createTempDir("agent");
				try {
					String agentVersion = OneDev.getInstance(AgentManager.class).getAgentVersion();
					
					File agentDir = new File(tempDir, "agent");
					String wrapperConfContent = FileUtils.readFileToString(
							new File(Bootstrap.installDir, "agent/wrapper.conf"), StandardCharsets.UTF_8);
					wrapperConfContent = wrapperConfContent.replace("agentVersion", agentVersion);
					FileUtils.writeStringToFile(new File(agentDir, "agent/conf/wrapper.conf"), 
							wrapperConfContent, StandardCharsets.UTF_8);
					
					FileUtils.copyFile(new File(Bootstrap.installDir, "agent/wrapper-license.conf"), 
							new File(agentDir, "agent/conf/wrapper-license.conf"));
					FileUtils.copyFile(new File(Bootstrap.installDir, "agent/App.sh.in"), 
							new File(agentDir, "agent/bin/agent.sh"));
					FileUtils.copyFile(new File(Bootstrap.installDir, "agent/AppCommand.bat.in"), 
							new File(agentDir, "agent/bin/agent.bat"));
					
					Properties props = new Properties();
					props.setProperty("serverUrl", OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl());
					
					AgentToken token = new AgentToken();
					token.setNote("Token for downloaded agent");
					token.setValue(UUID.randomUUID().toString());
					OneDev.getInstance(AgentTokenManager.class).save(token);
					props.setProperty("agentToken", token.getValue());
					
					try (OutputStream os = new FileOutputStream(new File(agentDir, "agent/conf/agent.properties"))) {
						String comment = String.format(""
								+ " %s: required property to specify url of OneDev server\n"
								+ " %s: required property to authenticate to OneDev server.\n"
								+ "     Will be generated automatically when agent is\n"
								+ "     downloaded. Can be generated manually from agent\n"
								+ "     management page if desired\n"
								+ " %s: optional property to specify name of the agent.\n"
								+ "     Use host name if omitted\n"
								+ " %s: optional property to specify cpu capability of the\n"
								+ "     agent in millis. It is normally (cpu cores)*1000.\n"
								+ "     Omitted to detect automatically\n"
								+ " %s: optional property to specify physical memory of the\n"
								+ "     agent in mega bytes. Omitted to detect automatically\n"
								+ " %s: optional property to specify path to git command\n"
								+ "     line. Omitted to search in system path\n"
								+ " %s: optional property to specify path to docker command\n"
								+ "     line. Omitted to search in system path\n"
								+ "", 
								Agent.SERVER_URL_KEY, Agent.AGENT_TOKEN_KEY, Agent.AGENT_NAME_KEY, 
								Agent.AGENT_CPU_KEY, Agent.AGENT_MEMORY_KEY, Agent.GIT_PATH_KEY, Agent.DOCKER_PATH_KEY);
						props.store(os, comment);
					}
					
					try (
							InputStream is = Agent.class.getClassLoader().getResourceAsStream("agent/conf/logback.xml");
							OutputStream os = new FileOutputStream(new File(agentDir, "agent/conf/logback.xml"));) {
						IOUtils.copy(is, os);
					}
					FileUtils.touchFile(new File(agentDir, "agent/conf/attributes.properties"));
					FileUtils.touchFile(new File(agentDir, "agent/logs/console.log"));
					FileUtils.touchFile(new File(agentDir, "agent/status/do_not_remove.txt"));
					
					Collection<String> agentLibs = OneDev.getInstance(AgentManager.class).getAgentLibs();
					
					for (File file: Bootstrap.getBootDir().listFiles()) {
						if (file.getName().startsWith("libwrapper-") 
								|| file.getName().startsWith("wrapper-") 
								|| file.getName().equals("wrapper.jar")) {
							FileUtils.copyFileToDirectory(file, new File(agentDir, "agent/boot"));
						}
					}
					
					for (File file: Bootstrap.getBootDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, new File(agentDir, "agent/lib/" + agentVersion));
					}
					for (File file: Bootstrap.getLibDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, new File(agentDir, "agent/lib/" + agentVersion));
					}
					
					File zipFile = new File(tempDir, "agent.zip");
					FileUtils.zip(agentDir, zipFile, "agent/boot/wrapper-*, agent/bin/*.sh");
					IOUtils.copy(zipFile, attributes.getResponse().getOutputStream());
				} finally {
					FileUtils.deleteDir(tempDir);
				}
			}				
			
		});

		return response;
	}

}
