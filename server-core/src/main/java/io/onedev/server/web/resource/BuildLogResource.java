package io.onedev.server.web.resource;

import static io.onedev.server.util.IOUtils.BUFFER_SIZE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.logging.BuildLoggingIdentity;
import io.onedev.server.logging.LogService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.IOUtils;

public class BuildLogResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_BUILD = "build";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		Long projectId = params.get(PARAM_PROJECT).toLong();
		Long buildNumber = params.get(PARAM_BUILD).toOptionalLong();
		if (buildNumber == null)
			throw new IllegalArgumentException("build number has to be specified");

		if (!SecurityUtils.isSystem()) {
			Project project = getProjectService().load(projectId);			
			Build build = getBuildService().find(project, buildNumber);

			if (build == null) {
				String message = String.format("Unable to find build (project: %s, build number: %d)", 
						project.getPath(), buildNumber);
				throw new EntityNotFoundException(message);
			}
			
			if (!SecurityUtils.canAccessLog(build))
				throw new UnauthorizedException();
		}
		
		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			response.setFileName(URLEncoder.encode("build-log.txt", StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				String activeServer = getProjectService().getActiveServer(projectId, true);
				var clusterService = getClusterService();
				if (activeServer.equals(clusterService.getLocalServerAddress())) {
					var loggingIdentity = new BuildLoggingIdentity(projectId, buildNumber);
					try (
							InputStream is = getLogService().openLogStream(loggingIdentity);
							OutputStream os = attributes.getResponse().getOutputStream()) {
						IOUtils.copy(is, os, BUFFER_SIZE);
					}
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				CharSequence path = RequestCycle.get().urlFor(
	    						new BuildLogResourceReference(), 
	    						BuildLogResource.paramsOf(projectId, buildNumber));
	    				String activeServerUrl = clusterService.getServerUrl(activeServer) + path;
	    				
	    				WebTarget target = client.target(activeServerUrl).path(path.toString());
	    				Invocation.Builder builder =  target.request();
	    				builder.header(HttpHeaders.AUTHORIZATION, 
	    						KubernetesHelper.BEARER + " " + clusterService.getCredential());
	    				
	    				try (Response response = builder.get()) {
	    					KubernetesHelper.checkStatus(response);
	    					try (
	    							InputStream is = response.readEntity(InputStream.class);
	    							OutputStream os = attributes.getResponse().getOutputStream()) {
	    						IOUtils.copy(is, os, BUFFER_SIZE);
	    					} 
	    				} 
	    			} finally {
	    				client.close();
	    			}
				}
			}			
			
		});

		return response;
	}

	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

	private LogService getLogService() {
		return OneDev.getInstance(LogService.class);
	}

	private BuildService getBuildService() {
		return OneDev.getInstance(BuildService.class);
	}

	public static PageParameters paramsOf(Long projectId, Long buildNumber) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, projectId);
		params.set(PARAM_BUILD, buildNumber);
		return params;
	}
	
}
