package io.onedev.server.web.resource;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.LfsObject;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.LongRange;
import io.onedev.server.web.mapper.ProjectMapperUtils;
import io.onedev.server.web.util.MimeUtils;
import io.onedev.server.web.util.WicketUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RawBlobResource extends AbstractResource {

	private static final long serialVersionUID = 1L;
	
	private static final String PARAM_DISPOSITION = "disposition";
	
	private static final Logger logger = LoggerFactory.getLogger(RawBlobResource.class);

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectPath = params.get(ProjectMapperUtils.PARAM_PROJECT).toString();
		Project project = getProjectService().findByPath(projectPath);
		if (project == null)
			throw new EntityNotFoundException("Project not found: " + projectPath);
		
		List<String> revisionAndPathSegments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
			if (segment.contains(".."))
				throw new ExplicitException("Invalid request path");
			if (segment.length() != 0)
				revisionAndPathSegments.add(segment);
		}

		BlobIdent blobIdent = new BlobIdent(project, revisionAndPathSegments);

		String revision = blobIdent.revision;
		String path = blobIdent.path;
		if (StringUtils.isBlank(revision) || StringUtils.isBlank(path))
			throw new IllegalArgumentException("Revision and path should be specified");

		if (!SecurityUtils.canReadCode(project))
			throw new UnauthorizedException();

		final Blob blob = project.getBlob(new BlobIdent(revision, path, 0), true);

		ResourceResponse response = new ResourceResponse();
		response.setAcceptRange(ContentRangeType.BYTES);
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.setContentType(MimeUtils.sanitize(project.detectMediaType(blob.getIdent()).toString()));
		
		if (blob.getLfsPointer() != null) 
			response.setContentLength(blob.getLfsPointer().getObjectSize());
		else 
			response.setContentLength(blob.getSize());
		
		if (!ObjectId.isId(revision))
			response.disableCaching();

		try {
			response.setFileName(URLEncoder.encode(blob.getIdent().getName(), StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		String disposition = params.get(PARAM_DISPOSITION).toOptionalString();
		if (disposition != null)
			response.setContentDisposition(ContentDisposition.valueOf(disposition));
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				try (InputStream is = getInputStream(blob)) {
					long contentLength;
					if (blob.getLfsPointer() != null)
						contentLength = blob.getLfsPointer().getObjectSize() - 1;
					else
						contentLength = blob.getSize() - 1;
					
					LongRange range = WicketUtils.getRequestContentRange(contentLength);
					try {
						IOUtils.copyRange(is, attributes.getResponse().getOutputStream(), range);
					} catch (Exception e) {
						EofException eofException = ExceptionUtils.find(e, EofException.class);
						if (eofException != null) 
							logger.trace("EOF while writing data", eofException);
						else 
							throw e;
					}
				}
			}

			private InputStream getInputStream(Blob blob) {
				if (blob.getLfsPointer() == null && !blob.isPartial()) {
					return new ByteArrayInputStream(blob.getBytes());
				} else {
					String activeServer = getProjectService().getActiveServer(project.getId(), true);
					if (activeServer.equals(getClusterService().getLocalServerAddress())) {
						if (blob.getLfsPointer() != null) {
							return new LfsObject(project.getId(), blob.getLfsPointer().getObjectId()).getInputStream();
						} else {
							Repository repository = getProjectService().getRepository(project.getId());
							ObjectId commitId = project.getObjectId(blob.getIdent().revision, true);
							return GitUtils.getInputStream(repository, commitId, blob.getIdent().path);
						}
					} else {
						// Use JDK api instead of jersey client api to get input stream directly as otherwise
						// jersey will throw exception when close the response
						try {
							var builder = new URIBuilder(getClusterService().getServerUrl(activeServer));
							if (blob.getLfsPointer() != null) {
								builder.setPath("~api/cluster/lfs")
										.addParameter("projectId", String.valueOf(project.getId()))
										.addParameter("objectId", blob.getLfsPointer().getObjectId());
							} else {
								ObjectId commitId = project.getObjectId(blob.getIdent().revision, true);
								builder.setPath("~api/cluster/blob")
										.addParameter("projectId", String.valueOf(project.getId()))
										.addParameter("revId", commitId.name())
										.addParameter("path", blob.getIdent().path);
							}
							var conn = builder.build().toURL().openConnection();
							conn.setRequestProperty(HttpHeaders.AUTHORIZATION,
									KubernetesHelper.BEARER + " " + getClusterService().getCredential());
							return conn.getInputStream();
						} catch (URISyntaxException | IOException e) {
							throw new RuntimeException(e);
						}
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

	public static PageParameters paramsOf(Project project, BlobIdent blobIdent) {
		return paramsOf(project, blobIdent, null);
	}
	
	public static PageParameters paramsOf(Project project, BlobIdent blobIdent, 
										  @Nullable ContentDisposition disposition) {
		PageParameters params = new PageParameters();
		params.set(ProjectMapperUtils.PARAM_PROJECT, project.getPath());
		if (disposition != null)
			params.add(PARAM_DISPOSITION, disposition.name());
		
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
