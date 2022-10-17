package io.onedev.server.web.resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static io.onedev.commons.bootstrap.Bootstrap.BUFFER_SIZE;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jgit.api.ArchiveCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.archive.TgzFormat;
import org.eclipse.jgit.archive.ZipFormat;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;

public class ArchiveResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_FORMAT = "format";
	
	public static final String FORMAT_ZIP = "zip";
	
	public static final String FORMAT_TGZ = "tgz";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		Long projectId = params.get(PARAM_PROJECT).toLong();
		
		String revision = params.get(PARAM_REVISION).toString();
		if (StringUtils.isBlank(revision))
			throw new IllegalArgumentException("revision parameter has to be specified");
		
		String format = params.get(PARAM_FORMAT).toString();
		if (!FORMAT_ZIP.equals(format) && !FORMAT_TGZ.equals(format)) {
			throw new IllegalArgumentException("format parameter should be specified either zip or tar.gz");
		}
		
		if (!SecurityUtils.getUserId().equals(User.SYSTEM_ID)) {
			// Perform database operations only if it is not a cluster access to avoid possible deadlocks
			Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
			if (!SecurityUtils.canReadCode(project)) 
				throw new UnauthorizedException();
		}
		
		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			String fileName;
			if (FORMAT_ZIP.equals(format))
				fileName = revision + ".zip";
			else
				fileName = revision + ".tar.gz";
			response.setFileName(URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, true);
				ClusterManager clusterManager = OneDev.getInstance(ClusterManager.class);
				if (storageServerUUID.equals(clusterManager.getLocalServerUUID())) {
					if (format.equals("zip"))
						ArchiveCommand.registerFormat(format, new ZipFormat());
					else
						ArchiveCommand.registerFormat(format, new TgzFormat());
					try {
						RevCommit commit;
						Repository repository = OneDev.getInstance(ProjectManager.class).getRepository(projectId);
						try (RevWalk revWalk = new RevWalk(repository)) {
							commit = revWalk.parseCommit(repository.resolve(revision));
						}
						ArchiveCommand archive = Git.wrap(repository).archive();
						archive.setFormat(format);
						archive.setTree(commit);
						archive.setOutputStream(attributes.getResponse().getOutputStream());
						archive.call();
					} catch (GitAPIException e) {
						throw new RuntimeException(e);
					} finally {
						ArchiveCommand.unregisterFormat(format);
					}
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				CharSequence path = RequestCycle.get().urlFor(
	    						new ArchiveResourceReference(), 
	    						ArchiveResource.paramsOf(projectId, revision, format));
	    				String storageServerUrl = clusterManager.getServerUrl(storageServerUUID) + path;
	    				
	    				WebTarget target = client.target(storageServerUrl).path(path.toString());
	    				Invocation.Builder builder =  target.request();
	    				builder.header(HttpHeaders.AUTHORIZATION, 
	    						KubernetesHelper.BEARER + " " + clusterManager.getCredentialValue());
	    				
	    				try (Response response = builder.get()) {
	    					KubernetesHelper.checkStatus(response);
	    					try (
	    							InputStream is = new BufferedInputStream(response.readEntity(InputStream.class), BUFFER_SIZE);
	    							OutputStream os = new BufferedOutputStream(attributes.getResponse().getOutputStream(), BUFFER_SIZE)) {
	    						IOUtils.copy(is, os);
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

	public static PageParameters paramsOf(Long projectId, String revision, String format) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, projectId);
		params.set(PARAM_REVISION, revision);
		params.set(PARAM_FORMAT, format);
		
		return params;
	}
	
}
