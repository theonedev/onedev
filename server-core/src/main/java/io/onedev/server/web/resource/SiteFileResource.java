package io.onedev.server.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.LockUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ExceptionUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.LongRange;
import io.onedev.server.util.MimeFileInfo;
import io.onedev.server.web.mapper.ProjectMapperUtils;
import io.onedev.server.web.util.WicketUtils;

public class SiteFileResource extends AbstractResource {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(SiteFileResource.class);

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectPath = params.get(ProjectMapperUtils.PARAM_PROJECT).toString();
		Project project = getProjectManager().findByPath(projectPath);
		if (project == null)
			throw new EntityNotFoundException();
		
		Long projectId = project.getId();
		
		if (!SecurityUtils.canAccess(project))
			throw new UnauthorizedException();

		List<String> filePathSegments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
			if (segment.length() != 0)
				filePathSegments.add(segment);
		}
		
		String filePath;
		if (!filePathSegments.isEmpty())
			filePath = Joiner.on("/").join(filePathSegments);
		else
			filePath = "index.html";

		ResourceResponse response = new ResourceResponse();
		response.setAcceptRange(ContentRangeType.BYTES);
		
		MimeFileInfo mimeFileInfo = getProjectManager().getSiteFileInfo(projectId, filePath);
		response.setContentType(mimeFileInfo.getMediaType());
		
		response.setContentLength(mimeFileInfo.getLength());
		
		try {
			response.setFileName(URLEncoder.encode(filePath, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		response.setWriteCallback(new WriteCallback() {

			private void handle(Exception e) {
				EofException eofException = ExceptionUtils.find(e, EofException.class);
				if (eofException != null) 
					logger.trace("EOF while writing data", eofException);
				else 
					throw ExceptionUtils.unchecked(e);
			}
			
			@Override
			public void writeData(Attributes attributes) throws IOException {
				LongRange range = WicketUtils.getRequestContentRange(mimeFileInfo.getLength());
				
				UUID storageServerUUID = getProjectManager().getStorageServerUUID(projectId, true);
				if (storageServerUUID.equals(getClusterManager().getLocalServerUUID())) {
					LockUtils.read(Project.getSiteLockName(projectId), new Callable<Void>() {

						@Override
						public Void call() {
							File file = new File(getStorageManager().getProjectSiteDir(projectId), filePath);
							try (InputStream is = new FileInputStream(file)) {
								IOUtils.copyRange(is, attributes.getResponse().getOutputStream(), range);
							} catch (IOException e) {
								handle(e);
							}
							return null;
						}
						
					});
				} else {
					Client client = ClientBuilder.newClient();
					try {
						String serverUrl = getClusterManager().getServerUrl(storageServerUUID);
						WebTarget target = client.target(serverUrl);
						target = target.path("~api/cluster/site")
								.queryParam("projectId", project.getId())
								.queryParam("filePath", filePath);
						Invocation.Builder builder =  target.request();
						builder.header(HttpHeaders.AUTHORIZATION, 
								KubernetesHelper.BEARER + " " + getClusterManager().getCredentialValue());
						try (Response response = builder.get()){
							KubernetesHelper.checkStatus(response);
							try (InputStream is = response.readEntity(InputStream.class)) {
								IOUtils.copyRange(is, attributes.getResponse().getOutputStream(), range);
							} catch (Exception e) {
								handle(e);
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

	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}
	
	private StorageManager getStorageManager() {
		return OneDev.getInstance(StorageManager.class);
	}
	
	public static PageParameters paramsOf(Project project, BlobIdent blobIdent) {
		PageParameters params = new PageParameters();
		params.set(ProjectMapperUtils.PARAM_PROJECT, project.getPath());
		
		int index = 0;
		for (String segment: Splitter.on("/").split(blobIdent.revision)) {
			params.set(index, segment);
			index++;
		}
		for (String segment: Splitter.on("/").split(blobIdent.path)) {
			params.set(index, segment);
			index++;
		}

		return params;
	}

}
