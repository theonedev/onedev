package io.onedev.server.web.resource;

import com.google.common.collect.Sets;
import io.onedev.agent.Agent;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.AgentToken;
import io.onedev.server.security.SecurityUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.resource.AbstractResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;

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
		
		String fileName = StringUtils.substringAfterLast(
				attributes.getRequest().getUrl().getPath(), "/");
		response.setFileName(fileName);
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				File tempDir = FileUtils.createTempDir("agent");
				try {
					String agentVersion = OneDev.getInstance(AgentManager.class).getAgentVersion();
					
					File agentDir = new File(tempDir, "agent");
					FileUtils.copyDirectoryToDirectory(new File(Bootstrap.installDir, "agent"), agentDir);
					
					String wrapperConfContent = FileUtils.readFileToString(
							new File(agentDir, "agent/conf/wrapper.conf"), StandardCharsets.UTF_8);
					wrapperConfContent = wrapperConfContent.replace("agentVersion", agentVersion);
					FileUtils.writeStringToFile(new File(agentDir, "agent/conf/wrapper.conf"), 
							wrapperConfContent, StandardCharsets.UTF_8);
					
					Properties props = new Properties();
					props.setProperty("serverUrl", OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl());
					
					AgentToken token = new AgentToken();
					OneDev.getInstance(AgentTokenManager.class).create(token);
					props.setProperty("agentToken", token.getValue());
					
					try (OutputStream os = new FileOutputStream(new File(agentDir, "agent/conf/agent.properties"))) {
						String comment = "For a list of supported agent properties, please visit:\n" 
								+ "https://docs.onedev.io/administration-guide/agent-management#agent-propertiesenvironments";
						props.store(os, comment);
					}
					
					try (
							InputStream is = Agent.class.getClassLoader().getResourceAsStream("agent/conf/logback.xml");
							OutputStream os = new FileOutputStream(new File(agentDir, "agent/conf/logback.xml"));) {
						IOUtils.copy(is, os);
					}
					FileUtils.touchFile(new File(agentDir, "agent/conf/attributes.properties"));
					FileUtils.touchFile(new File(agentDir, "agent/logs/console.log"));
					
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

					if (fileName.endsWith("zip")) {
						File packageFile = new File(tempDir, fileName);
						FileUtils.zip(agentDir, packageFile, "agent/boot/wrapper-*, agent/bin/*.sh");
						IOUtils.copy(packageFile, attributes.getResponse().getOutputStream());
					} else {
						FileUtils.tar(agentDir, Sets.newHashSet("**"), Sets.newHashSet(), 
								Sets.newHashSet("agent/boot/wrapper-*", "agent/bin/*.sh"),
								attributes.getResponse().getOutputStream(), true);
					}
				} finally {
					FileUtils.deleteDir(tempDir);
				}
			}				
			
		});

		return response;
	}

}
