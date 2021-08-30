package io.onedev.server.web.resource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpHeaders;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.collect.Lists;

import io.onedev.agent.Agent;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.AgentToken;

public class AgentLibResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();
		String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (bearer != null && bearer.startsWith(Agent.BEARER + " ")) {
			String tokenValue = bearer.substring(Agent.BEARER.length() + 1);
			AgentToken token = OneDev.getInstance(AgentTokenManager.class).find(tokenValue);
			if (token == null) 
				throw new ExplicitException("Invalid agent token");
		} else {
			throw new ExplicitException("No agent token");
		}
		
		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				File tempDir = FileUtils.createTempDir("agent-lib");
				try {
					Collection<String> agentLibs = OneDev.getInstance(AgentManager.class).getAgentLibs();
					
					for (File file: Bootstrap.getBootDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, tempDir);
					}
					
					for (File file: Bootstrap.getLibDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, tempDir);
					}
					
					OutputStream os = attributes.getResponse().getOutputStream();
					FileUtils.tar(tempDir, Lists.newArrayList("**"), new ArrayList<>(), os, false);
				} finally {
					FileUtils.deleteDir(tempDir);
				}
			}				
			
		});

		return response;
	}

}
