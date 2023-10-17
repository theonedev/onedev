package io.onedev.server.web.resource;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.web.util.MimeUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.commons.bootstrap.Bootstrap.BUFFER_SIZE;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.model.Build.getArtifactsLockName;

public class ArtifactResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";

	private static final String PARAM_BUILD = "build";

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		Long projectId = params.get(PARAM_PROJECT).toLong();
		Long buildNumber = params.get(PARAM_BUILD).toLong();
		
		List<String> pathSegments = new ArrayList<>();

		for (int i = 0; i < params.getIndexedCount(); i++) {
			String pathSegment = params.get(i).toString();
			if (pathSegment.length() != 0)
				pathSegments.add(pathSegment);
		}
		
		if (pathSegments.isEmpty())
			throw new ExplicitException("Artifact path has to be specified");
		
		String artifactPath = Joiner.on("/").join(pathSegments);
		
		FileInfo fileInfo = null;
		if (!SecurityUtils.getUserId().equals(User.SYSTEM_ID)) {
			Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
			
			Build build = OneDev.getInstance(BuildManager.class).find(project, buildNumber);

			if (build == null) {
				String message = String.format("Unable to find build (project: %s, build number: %d)", 
						project.getPath(), buildNumber);
				throw new EntityNotFoundException(message);
			}
			
			if (!SecurityUtils.canAccess(build))
				throw new UnauthorizedException();
			
			fileInfo = (FileInfo) getBuildManager().getArtifactInfo(build, artifactPath);
		}
		
		ResourceResponse response = new ResourceResponse();
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.disableCaching();

		String fileName = artifactPath;
		if (fileName.contains("/"))
			fileName = StringUtils.substringAfterLast(fileName, "/");
		try {
			response.setFileName(URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		if (fileInfo != null) {
			response.setContentLength(fileInfo.getLength());
			response.setContentType(MimeUtils.sanitize(fileInfo.getMediaType()));
		} else {
			response.setContentType(MimeTypes.OCTET_STREAM);
		}
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				String activeServer = projectManager.getActiveServer(projectId, true);
				ClusterManager clusterManager = OneDev.getInstance(ClusterManager.class);
				if (activeServer.equals(clusterManager.getLocalServerAddress())) {
					read(getArtifactsLockName(projectId, buildNumber), () -> {
						File artifactFile = new File(Build.getArtifactsDir(projectId, buildNumber), artifactPath);
						try (
								InputStream is = new FileInputStream(artifactFile);
								OutputStream os = attributes.getResponse().getOutputStream()) {
							IOUtils.copy(is, os, BUFFER_SIZE);
						}
						return null;
					});
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				CharSequence path = RequestCycle.get().urlFor(
	    						new ArtifactResourceReference(), 
	    						ArtifactResource.paramsOf(projectId, buildNumber, artifactPath));
	    				String activeServerUrl = clusterManager.getServerUrl(activeServer);
	    				
	    				WebTarget target = client.target(activeServerUrl).path(path.toString());
	    				Invocation.Builder builder =  target.request();
	    				builder.header(HttpHeaders.AUTHORIZATION, 
	    						KubernetesHelper.BEARER + " " + clusterManager.getCredential());
	    				
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
	
	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}
	
	public static PageParameters paramsOf(Long projectId, Long buildNumber, String path) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, projectId);
		params.set(PARAM_BUILD, buildNumber);
		
		int index = 0;
		for (String segment: Splitter.on("/").split(path)) {
			params.set(index, segment);
			index++;
		}
		return params;
	}

}
