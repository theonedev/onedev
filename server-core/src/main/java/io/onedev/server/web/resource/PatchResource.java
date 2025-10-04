package io.onedev.server.web.resource;

import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;

public class PatchResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_OLD_COMMIT = "old-commit";

	private static final String PARAM_NEW_COMMIT = "new-commit";
		
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		
		Long projectId = params.get(PARAM_PROJECT).toLong();
		var oldCommitId = ObjectId.fromString(params.get(PARAM_OLD_COMMIT).toString());
		var newCommitId = ObjectId.fromString(params.get(PARAM_NEW_COMMIT).toString());
		
		if (!SecurityUtils.isSystem()) {
			Project project = OneDev.getInstance(ProjectService.class).load(projectId);
			if (!SecurityUtils.canReadCode(project))
				throw new UnauthorizedException();
		}

		ResourceResponse response = new ResourceResponse();
		
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.setContentType(MimeTypes.OCTET_STREAM);

		response.setFileName(URLEncoder.encode("changes.patch", UTF_8));

		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				String activeServer = getProjectService().getActiveServer(projectId, true);
				ClusterService clusterService = OneDev.getInstance(ClusterService.class);
				if (activeServer.equals(clusterService.getLocalServerAddress())) {
					try (var os = attributes.getResponse().getOutputStream()) {
						var repository = getProjectService().getRepository(projectId);
						GitUtils.diff(repository, oldCommitId, newCommitId, os);
					}
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				String activeServerUrl = clusterService.getServerUrl(activeServer);
	    				var pathAndQuery = Url.parse(RequestCycle.get().urlFor(
	    						new PatchResourceReference(), 
	    						PatchResource.paramsOf(projectId, oldCommitId, newCommitId)));
							    				
	    				WebTarget target = client.target(activeServerUrl).path(pathAndQuery.getPath());
						for (var entry: pathAndQuery.getQueryParameters()) 
							target = target.queryParam(entry.getName(), entry.getValue());
	    				Invocation.Builder builder =  target.request();
	    				builder.header(HttpHeaders.AUTHORIZATION, 
	    						KubernetesHelper.BEARER + " " + clusterService.getCredential());
	    				
	    				try (Response response = builder.get()) {
	    					KubernetesHelper.checkStatus(response);
	    					try (
	    							var is = response.readEntity(InputStream.class);
	    							var os = attributes.getResponse().getOutputStream()) {
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
	
	public static PageParameters paramsOf(Long projectId, ObjectId oldCommitId, ObjectId newCommitId) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PROJECT, projectId);
		params.add(PARAM_OLD_COMMIT, oldCommitId.name());
		params.set(PARAM_NEW_COMMIT, newCommitId.name());
		
		return params;
	}
	
}
