package io.onedev.server.web.resource;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.LockUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ExceptionUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.LongRange;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.web.mapper.ProjectMapperUtils;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

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
		
		List<String> filePathSegments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
			if (segment.length() != 0)
				filePathSegments.add(segment);
		}
		
		FileInfo fileInfo;
		String filePath = Joiner.on("/").join(filePathSegments);
		if (filePath.length() != 0) {
			ArtifactInfo artifactInfo = getProjectManager().getSiteArtifactInfo(projectId, filePath);
			if (artifactInfo instanceof DirectoryInfo) {
				if (attributes.getRequest().getUrl().getPath().endsWith("/")) {
					filePath += "/index.html";
					artifactInfo = getProjectManager().getSiteArtifactInfo(projectId, filePath);
					if (artifactInfo instanceof FileInfo)
						fileInfo = (FileInfo) artifactInfo;
					else						
						return newNotFoundResponse(filePath);
				} else {
					throw new RedirectToUrlException(attributes.getRequest().getUrl().getPath() + "/");
				}
			} else if (artifactInfo instanceof FileInfo) {
				fileInfo = (FileInfo) artifactInfo;
			} else {
				return newNotFoundResponse(filePath);
			}
		} else if (attributes.getRequest().getUrl().getPath().endsWith("/")) {
			filePath = "index.html";
			ArtifactInfo artifactInfo = getProjectManager().getSiteArtifactInfo(projectId, filePath);
			if (artifactInfo instanceof FileInfo)
				fileInfo = (FileInfo) artifactInfo;
			else
				return newNotFoundResponse(filePath);
		} else {
			throw new RedirectToUrlException(attributes.getRequest().getUrl().getPath() + "/");
		}
		
		ResourceResponse response = new ResourceResponse();
		response.setAcceptRange(ContentRangeType.BYTES);
		response.setContentType(fileInfo.getMediaType());
		response.setContentLength(fileInfo.getLength());
		
		try {
			response.setFileName(URLEncoder.encode(filePath, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		String finalFilePath = filePath;
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
				LongRange range = WicketUtils.getRequestContentRange(fileInfo.getLength());
				
				UUID storageServerUUID = getProjectManager().getStorageServerUUID(projectId, true);
				if (storageServerUUID.equals(getClusterManager().getLocalServerUUID())) {
					LockUtils.read(Project.getSiteLockName(projectId), new Callable<Void>() {

						@Override
						public Void call() {
							File file = new File(getStorageManager().getProjectSiteDir(projectId), finalFilePath);
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
								.queryParam("filePath", finalFilePath);
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
	
	private ResourceResponse newNotFoundResponse(String filePath) {
		ResourceResponse response = new ResourceResponse();
		response.setStatusCode(HttpServletResponse.SC_NOT_FOUND).setContentType(MediaType.TEXT_PLAIN);
		return new ResourceResponse().setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(Attributes attributes) throws IOException {
				attributes.getResponse().write("Site file not found: " + filePath);
			}
			
		});
				
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
