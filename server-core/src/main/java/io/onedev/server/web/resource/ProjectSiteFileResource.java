package io.onedev.server.web.resource;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.util.IOUtils.copyRange;
import static io.onedev.server.util.SiteSyncUtils.FILE_VERSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Joiner;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.LongRange;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.validation.validator.UrlSegmentValidator;
import io.onedev.server.web.mapper.ProjectMapperUtils;
import io.onedev.server.web.util.WicketUtils;

public class ProjectSiteFileResource extends AbstractResource {

	private static final String PARAM_SITE = "site";

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectSiteFileResource.class);

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectPath = params.get(ProjectMapperUtils.PARAM_PROJECT).toString();
		Project project = getProjectService().findByPath(projectPath);
		if (project == null)
			throw new EntityNotFoundException();
		
		Long projectId = project.getId();
		var siteName = params.get(PARAM_SITE).toString("");
		if (!UrlSegmentValidator.PATTERN.matcher(siteName).matches())
			throw new ExplicitException("Invalid site name");
		if (!SecurityUtils.canAccessSite(project, siteName))
			throw new UnauthorizedException();
		
		List<String> filePathSegments = new ArrayList<>();
		filePathSegments.add(siteName);
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
			if (segment.contains(".."))
				throw new ExplicitException("Invalid request path");
			if (segment.length() != 0)
				filePathSegments.add(segment);
		}
		
		FileInfo fileInfo;
		String filePath = Joiner.on("/").join(filePathSegments);
		if (filePathSegments.contains(FILE_VERSION))
			return newNotFoundResponse(filePath);
		ArtifactInfo artifactInfo = getProjectService().getSiteArtifactInfo(projectId, filePath);
		if (artifactInfo instanceof DirectoryInfo directoryInfo) {
			if (!attributes.getRequest().getUrl().getPath().endsWith("/"))
				throw new RedirectToUrlException(attributes.getRequest().getUrl().getPath() + "/");
			String indexFilePath = filePath + "/index.html";
			artifactInfo = getProjectService().getSiteArtifactInfo(projectId, indexFilePath);
			if (artifactInfo instanceof FileInfo)
				fileInfo = (FileInfo) artifactInfo;
			else
				return newDirectoryResponse(filePath, directoryInfo);
			filePath = indexFilePath;
		} else if (artifactInfo instanceof FileInfo) {
			fileInfo = (FileInfo) artifactInfo;
		} else {
			return newNotFoundResponse(filePath);
		}

		ResourceResponse response = new ResourceResponse();
		response.disableCaching();
		response.setAcceptRange(ContentRangeType.BYTES);
		response.setContentType(fileInfo.getMediaType());
		response.setContentLength(fileInfo.getLength());
		
		response.setFileName(filePath.substring(filePath.lastIndexOf('/') + 1));

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
				
				String activeServer = getProjectService().getActiveServer(projectId, true);
				if (activeServer.equals(getClusterService().getLocalServerAddress())) {
					read(Project.getSitesLockName(projectId), () -> {
						File file = new File(getProjectService().getSitesDir(projectId), finalFilePath);
						try (InputStream is = new FileInputStream(file)) {
							copyRange(is, attributes.getResponse().getOutputStream(), range);
						} catch (IOException e) {
							handle(e);
						}
						return null;
					});
				} else {
					Client client = ClientBuilder.newClient();
					try {
						String serverUrl = getClusterService().getServerUrl(activeServer);
						WebTarget target = client.target(serverUrl);
						target = target.path("~api/cluster/project-sites")
								.queryParam("projectId", project.getId())
								.queryParam("filePath", finalFilePath);
						Invocation.Builder builder =  target.request();
						builder.header(HttpHeaders.AUTHORIZATION, 
								KubernetesHelper.BEARER + " " + getClusterService().getCredential());
						try (Response response = builder.get()){
							KubernetesHelper.checkStatus(response);
							try (InputStream is = response.readEntity(InputStream.class)) {
								copyRange(is, attributes.getResponse().getOutputStream(), range);
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

	private ResourceResponse newDirectoryResponse(String directoryPath, DirectoryInfo directoryInfo) {
		ResourceResponse response = new ResourceResponse();
		response.disableCaching();
		response.setContentType(MediaType.TEXT_HTML + "; charset=" + StandardCharsets.UTF_8.name());
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				String displayPath = "/" + directoryPath;
				if (!displayPath.endsWith("/"))
					displayPath += "/";
				String escapedDisplayPath = HtmlEscape.escapeHtml5(displayPath);
				StringBuilder builder = new StringBuilder();
				builder.append("<!doctype html><html><head><meta charset=\"UTF-8\"><title>Index of ")
						.append(escapedDisplayPath)
						.append("</title></head><body><h1>Index of ")
						.append(escapedDisplayPath)
						.append("</h1><ul>");
				if (directoryPath.contains("/"))
					builder.append("<li><a href=\"../\">../</a></li>");
				for (ArtifactInfo child : directoryInfo.getChildren() != null
						? directoryInfo.getChildren() : Collections.<ArtifactInfo>emptyList()) {
					String childPath = child.getPath();
					String name = childPath.substring(childPath.lastIndexOf('/') + 1);
					if (name.equals(FILE_VERSION))
						continue;
					String encodedName = UrlEncoder.PATH_INSTANCE.encode(name, StandardCharsets.UTF_8);
					if (child instanceof DirectoryInfo) {
						name += "/";
						encodedName += "/";
					}
					builder.append("<li><a href=\"")
							.append(encodedName)
							.append("\">")
							.append(HtmlEscape.escapeHtml5(name))
							.append("</a></li>");
				}
				attributes.getResponse().write(builder.append("</ul></body></html>").toString());
			}

		});
		return response;
	}
	
	private ResourceResponse newNotFoundResponse(String filePath) {
		ResourceResponse response = new ResourceResponse();
		response.disableCaching();
		response.setStatusCode(HttpServletResponse.SC_NOT_FOUND).setContentType(MediaType.TEXT_PLAIN);
		response.setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(Attributes attributes) throws IOException {
				attributes.getResponse().write("Site file not found: " + filePath);
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
	
	public static PageParameters paramsOf(Project project, String siteName) {
		PageParameters params = new PageParameters();
		params.set(ProjectMapperUtils.PARAM_PROJECT, project.getPath());
		params.set(PARAM_SITE, siteName);
		return params;
	}

}
