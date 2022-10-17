/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.onedev.server.git;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.util.CollectionUtils;

public class LfsAuthenticateCommand implements Command, ServerSessionAware {

	static final String COMMAND_PREFIX = "git-lfs-authenticate";
	
	private static final Logger logger = LoggerFactory.getLogger(LfsAuthenticateCommand.class);
	
	private final String commandString;
	
    private OutputStream out;
    
    private OutputStream err;
    
    private ExitCallback callback;
    
	private ServerSession session;

	LfsAuthenticateCommand(String commandString) {
		this.commandString = commandString;
	}
	
    @Override
    public void setInputStream(InputStream in) {
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
    	this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
    	SshAuthenticator authenticator = OneDev.getInstance(SshAuthenticator.class);
    	Long userId = authenticator.getPublicKeyOwnerId(session);
    	OneDev.getInstance(ExecutorService.class).submit(new Runnable() {

			@Override
			public void run() {
				SessionManager sessionManager = OneDev.getInstance(SessionManager.class);
		        sessionManager.openSession(); 
		        try {
		        	String accessToken = OneDev.getInstance(UserManager.class).load(userId).getAccessToken();
		        	String projectPath = StringUtils.strip(StringUtils.substringBefore(
		        			commandString.substring(COMMAND_PREFIX.length()+1), " "), "/\\");
		        	Project project = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
		        	if (project == null)
		        		throw new ExplicitException("Project not found: " + projectPath);
		        	String url = OneDev.getInstance(UrlManager.class).cloneUrlFor(project, false);
		        	Map<Object, Object> response = CollectionUtils.newHashMap(
		        			"href", url + ".git/info/lfs", 
		        			"header", CollectionUtils.newHashMap(
		        					"Authorization", KubernetesHelper.BEARER + " " + accessToken)); 
		        	out.write(OneDev.getInstance(ObjectMapper.class).writeValueAsBytes(response));
		        	callback.onExit(0);
		        } catch (Exception e) {
		        	logger.error("Error executing " + COMMAND_PREFIX, e);
		    		new PrintStream(err).println("Check server log for details");
		    		callback.onExit(-1);
		        } finally {                
		            sessionManager.closeSession();
		        }
			}
    		
    	});
    }

    @Override
    public void destroy(ChannelSession channel) {
    	
    }

	@Override
	public void setSession(ServerSession session) {
		this.session = session;
	}

}
