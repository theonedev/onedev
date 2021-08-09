package io.onedev.server.web.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.collect.Lists;

import io.onedev.agent.Agent;
import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.bootstrap.BootstrapUtils;
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

		String path = RequestCycle.get().getRequest().getUrl().getPath();
		boolean zipFormat = path.endsWith(".zip");
		
		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			String fileName = StringUtils.substringAfterLast(path, "/");
			response.setFileName(URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				File tempDir = FileUtils.createTempDir("agent");
				try {
					String agentVersion = OneDev.getInstance(AgentManager.class).getAgentVersion();
					
					String wrapperConfContent = FileUtils.readFileToString(
							new File(Bootstrap.installDir, "agent/wrapper.conf"), StandardCharsets.UTF_8);
					wrapperConfContent = wrapperConfContent.replace("agentVersion", agentVersion);
					FileUtils.writeStringToFile(new File(tempDir, "agent/conf/wrapper.conf"), 
							wrapperConfContent, StandardCharsets.UTF_8);
					
					FileUtils.copyFile(new File(Bootstrap.installDir, "agent/wrapper-license.conf"), 
							new File(tempDir, "agent/conf/wrapper-license.conf"));
					FileUtils.copyFile(new File(Bootstrap.installDir, "agent/App.sh.in"), 
							new File(tempDir, "agent/bin/agent.sh"));
					FileUtils.copyFile(new File(Bootstrap.installDir, "agent/AppCommand.bat.in"), 
							new File(tempDir, "agent/bin/agent.bat"));
					
					new File(tempDir, "agent/bin/agent.sh").setExecutable(true);
					
					Properties props = new Properties();
					props.setProperty("serverUrl", OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl());
					
					AgentToken token = new AgentToken();
					token.setNote("Token for downlaoded agent");
					token.setValue(UUID.randomUUID().toString());
					OneDev.getInstance(AgentTokenManager.class).save(token);
					props.setProperty("agentToken", token.getValue());
					
					try (OutputStream os = new FileOutputStream(new File(tempDir, "agent/conf/agent.properties"))) {
						props.store(os, "");
					}
					
					try (
							InputStream is = Agent.class.getClassLoader().getResourceAsStream("agent/conf/logback.xml");
							OutputStream os = new FileOutputStream(new File(tempDir, "agent/conf/logback.xml"));) {
						IOUtils.copy(is, os);
					}
					FileUtils.touchFile(new File(tempDir, "agent/conf/attributes.properties"));
					FileUtils.touchFile(new File(tempDir, "agent/logs/console.log"));
					FileUtils.touchFile(new File(tempDir, "agent/status/do_not_remove.txt"));
					
					Collection<String> agentLibs = OneDev.getInstance(AgentManager.class).getAgentLibs();
					
					for (File file: Bootstrap.getBootDir().listFiles()) {
						if (file.getName().startsWith("libwrapper-") 
								|| file.getName().startsWith("wrapper-") 
								|| file.getName().equals("wrapper.jar")) {
							FileUtils.copyFileToDirectory(file, new File(tempDir, "agent/boot"));
							if (file.getName().startsWith("wrapper-") && !file.getName().startsWith("wrapper-windows-"))
								new File(tempDir, "agent/boot/" + file.getName()).setExecutable(true);
						}
					}
					
					for (File file: Bootstrap.getBootDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, new File(tempDir, "agent/lib/" + agentVersion));
					}
					for (File file: Bootstrap.getLibDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, new File(tempDir, "agent/lib/" + agentVersion));
					}
					
					OutputStream os = attributes.getResponse().getOutputStream();
					if (zipFormat)
						BootstrapUtils.zip(tempDir, os);
					else
						FileUtils.tar(tempDir, Lists.newArrayList("**"), new ArrayList<>(), os, true);
				} finally {
					FileUtils.deleteDir(tempDir);
				}
			}				
			
		});

		return response;
	}

}
