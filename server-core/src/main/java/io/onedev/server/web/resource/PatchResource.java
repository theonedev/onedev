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
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.IOUtils;

public class PatchResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_OLD_COMMIT = "old-commit";

	private static final String PARAM_NEW_COMMIT = "new-commit";

	private static final String PARAM_FOR_CODE_REVIEW = "for-code-review";
		
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		
		Long projectId = params.get(PARAM_PROJECT).toLong();
		var oldCommitId = ObjectId.fromString(params.get(PARAM_OLD_COMMIT).toString());
		var newCommitId = ObjectId.fromString(params.get(PARAM_NEW_COMMIT).toString());
		var forCodeReview = params.get(PARAM_FOR_CODE_REVIEW).toBoolean(false);
		
		String excludedFiles;
		if (forCodeReview) {
			Project project = getProjectService().load(projectId);
			excludedFiles = project.findExcludedAiReviewFiles();
		} else {
			excludedFiles = null;
		}
		if (!SecurityUtils.isSystem()) {
			Project project = getProjectService().load(projectId);
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
				if (activeServer.equals(getClusterService().getLocalServerAddress())) {
					try (var os = attributes.getResponse().getOutputStream()) {
						var repository = getProjectService().getRepository(projectId);
						GitUtils.diff(repository, oldCommitId, newCommitId, excludedFiles, os);
					}
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				String activeServerUrl = getClusterService().getServerUrl(activeServer);
	    				var pathAndQuery = Url.parse(RequestCycle.get().urlFor(
	    						new PatchResourceReference(), 
	    						PatchResource.paramsOf(projectId, oldCommitId, newCommitId, forCodeReview)));
							    				
	    				WebTarget target = client.target(activeServerUrl).path(pathAndQuery.getPath());
						for (var entry: pathAndQuery.getQueryParameters()) 
							target = target.queryParam(entry.getName(), entry.getValue());
	    				Invocation.Builder builder =  target.request();
	    				builder.header(HttpHeaders.AUTHORIZATION, 
	    						KubernetesHelper.BEARER + " " + getClusterService().getCredential());
	    				
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

	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
		
	public static PageParameters paramsOf(Long projectId, ObjectId oldCommitId, ObjectId newCommitId) {
		return paramsOf(projectId, oldCommitId, newCommitId, false);
	}

	public static PageParameters paramsOf(Long projectId, ObjectId oldCommitId, ObjectId newCommitId, boolean forCodeReview) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PROJECT, projectId);
		params.add(PARAM_OLD_COMMIT, oldCommitId.name());
		params.set(PARAM_NEW_COMMIT, newCommitId.name());
		params.set(PARAM_FOR_CODE_REVIEW, forCodeReview);

		return params;
	}
	
}
