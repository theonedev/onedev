package io.onedev.server.web.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.LfsObject;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ExceptionUtils;
import io.onedev.server.web.mapper.ProjectMapperUtils;

public class RawBlobResource extends AbstractResource {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(RawBlobResource.class);

	private static final int BUFFER_SIZE = 8*1024;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectPath = params.get(ProjectMapperUtils.PARAM_PROJECT).toString();
		Project project = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
		if (project == null)
			throw new EntityNotFoundException();
		
		List<String> revisionAndPathSegments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
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
		response.setContentType(project.detectMediaType(blob.getIdent()).toString());
		
		if (blob.getLfsPointer() != null) 
			response.setContentLength(blob.getLfsPointer().getObjectSize());
		else 
			response.setContentLength(blob.getSize());
		
		if (response.getContentType().equals(MediaType.TEXT_HTML)) 
			response.setContentType(MediaType.TEXT_PLAIN);

		if (!ObjectId.isId(revision))
			response.disableCaching();

		try {
			response.setFileName(URLEncoder.encode(blob.getIdent().getName(), StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		response.setWriteCallback(new WriteCallback() {

			private void copyRange(InputStream in, OutputStream out, long start, long end) throws IOException {
				int totalSkipped = 0;
				while (totalSkipped < start)	 {
					long skipped = in.skip(start-totalSkipped);
					if (skipped == 0)
						break;
					totalSkipped += skipped;
				}
				
				if (totalSkipped < start) 
					throw new IOException("Skipped only " + totalSkipped + " bytes out of " + start + " required.");

				long bytesToCopy = end - start + 1;

				byte buffer[] = new byte[BUFFER_SIZE];
				while (bytesToCopy > 0) {
					int bytesRead = in.read(buffer);
					if (bytesRead <= bytesToCopy) {
						out.write(buffer, 0, bytesRead);
						bytesToCopy -= bytesRead;
					} else {
						out.write(buffer, 0, (int) bytesToCopy);
						bytesToCopy = 0;
					}
					if (bytesRead < buffer.length) {
						break;
					}
				}
			}

			@Override
			public void writeData(Attributes attributes) throws IOException {
				try (InputStream is = getInputStream(blob)) {
					Long startByte = RequestCycle.get().getMetaData(CONTENT_RANGE_STARTBYTE);
					Long endByte = RequestCycle.get().getMetaData(CONTENT_RANGE_ENDBYTE);

					if (startByte == null)
						startByte = 0L;
					if (endByte == null || endByte == -1) {
						if (blob.getLfsPointer() != null)
							endByte = blob.getLfsPointer().getObjectSize() - 1;
						else
							endByte = blob.getSize() - 1;
					}
					try {
						copyRange(is, attributes.getResponse().getOutputStream(), startByte, endByte);
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
					UUID storageServerUUID = getProjectManager().getStorageServerUUID(project.getId(), true);
					if (storageServerUUID.equals(getClusterManager().getLocalServerUUID())) {
						if (blob.getLfsPointer() != null) {
							return new LfsObject(project.getId(), blob.getLfsPointer().getObjectId()).getInputStream();
						} else {
							Repository repository = getProjectManager().getRepository(project.getId());
							ObjectId commitId = project.getObjectId(blob.getIdent().revision, true);
							return GitUtils.getInputStream(repository, commitId, blob.getIdent().path);
						}
					} else {
						Client client = ClientBuilder.newClient();
						try {
							String serverUrl = getClusterManager().getServerUrl(storageServerUUID);
							WebTarget target = client.target(serverUrl);
							if (blob.getLfsPointer() != null) {
								target = target.path("~api/cluster/lfs")
										.queryParam("projectId", project.getId())
										.queryParam("objectId", blob.getLfsPointer().getObjectId());
							} else {
								ObjectId commitId = project.getObjectId(blob.getIdent().revision, true);
								target = target.path("~api/cluster/blob")
										.queryParam("projectId", project.getId())
										.queryParam("revId", commitId.name())
										.queryParam("path", blob.getIdent().path);
							}
							Invocation.Builder builder =  target.request();
							builder.header(HttpHeaders.AUTHORIZATION, 
									KubernetesHelper.BEARER + " " + getClusterManager().getCredentialValue());
							try (Response response = builder.get()){
								KubernetesHelper.checkStatus(response);
								return response.readEntity(InputStream.class);
							}
						} finally {
							client.close();
						}
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
