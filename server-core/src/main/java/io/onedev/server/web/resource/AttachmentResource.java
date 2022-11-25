package io.onedev.server.web.resource;

import static io.onedev.commons.bootstrap.Bootstrap.BUFFER_SIZE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_ATTACHMENT_GROUP = "attachment-group";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	public static final String PARAM_AUTHORIZATION = "authorization";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		
		Long projectId = params.get(PARAM_PROJECT).toLong();
		String attachmentGroup = params.get(PARAM_ATTACHMENT_GROUP).toString();
		
		if (StringUtils.isBlank(attachmentGroup))
			throw new IllegalArgumentException("Parameter 'attachment-group' has to be specified");
		else if (attachmentGroup.contains(".."))
			throw new IllegalArgumentException("Invalid parameter 'attachment-group'");

		if (!SecurityUtils.getUserId().equals(User.SYSTEM_ID)) {
			Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
			
			String authorization = params.get(PARAM_AUTHORIZATION).toOptionalString();
			if (authorization == null 
					|| !new String(CryptoUtils.decrypt(Base64.decodeBase64(authorization)), StandardCharsets.UTF_8).equals(attachmentGroup)) {
				Issue issue;
				Build build;
				if (OneDev.getInstance(PullRequestManager.class).findByUUID(attachmentGroup) != null 
						|| OneDev.getInstance(CodeCommentManager.class).findByUUID(attachmentGroup) != null) {
					if (!SecurityUtils.canReadCode(project))
						throw new UnauthorizedException();
				} else if ((issue = OneDev.getInstance(IssueManager.class).findByUUID(attachmentGroup)) != null) {
					if (!SecurityUtils.canAccess(issue))
						throw new UnauthorizedException();
				} else if ((build = OneDev.getInstance(BuildManager.class).findByUUID(attachmentGroup)) != null) {
					if (!SecurityUtils.canAccess(build))
						throw new UnauthorizedException();
				} else if (!SecurityUtils.canAccess(project)) {
					throw new UnauthorizedException();
				}
			}
		}

		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");
		else if (attachment.contains(".."))
			throw new IllegalArgumentException("Invalid attachment parameter");

		ResourceResponse response = new ResourceResponse();
		response.setContentLength(getAttachmentManager().getAttachmentInfo(projectId, attachmentGroup, attachment).getLength());
		
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		try {
			response.setFileName(URLEncoder.encode(attachment, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				UUID storageServerUUID = getProjectManager().getStorageServerUUID(projectId, true);
				ClusterManager clusterManager = OneDev.getInstance(ClusterManager.class);
				if (storageServerUUID.equals(clusterManager.getLocalServerUUID())) {
					File attachmentFile = new File(getAttachmentManager().getAttachmentGroupDirLocal(projectId, attachmentGroup), attachment);
					try (
							InputStream is = new BufferedInputStream(
									new FileInputStream(attachmentFile), BUFFER_SIZE);
							OutputStream os = new BufferedOutputStream(
									attributes.getResponse().getOutputStream(), BUFFER_SIZE);) {
						IOUtils.copy(is, os);
					}
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				CharSequence path = RequestCycle.get().urlFor(
	    						new AttachmentResourceReference(), 
	    						AttachmentResource.paramsOf(projectId, attachmentGroup, attachment));
	    				String storageServerUrl = clusterManager.getServerUrl(storageServerUUID) + path;
	    				
	    				WebTarget target = client.target(storageServerUrl).path(path.toString());
	    				Invocation.Builder builder =  target.request();
	    				builder.header(HttpHeaders.AUTHORIZATION, 
	    						KubernetesHelper.BEARER + " " + clusterManager.getCredentialValue());
	    				
	    				try (Response response = builder.get()) {
	    					KubernetesHelper.checkStatus(response);
	    					try (
	    							InputStream is = new BufferedInputStream(
	    									response.readEntity(InputStream.class), BUFFER_SIZE);
	    							OutputStream os = new BufferedOutputStream(
	    									attributes.getResponse().getOutputStream(), BUFFER_SIZE)) {
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
		
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private static AttachmentManager getAttachmentManager() {
		return OneDev.getInstance(AttachmentManager.class);
	}

	public static PageParameters paramsOf(Long projectId, String attachmentGroup, String attachment) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, projectId);
		params.set(PARAM_ATTACHMENT_GROUP, attachmentGroup);
		params.set(PARAM_ATTACHMENT, attachment);
		
		params.set("v", getAttachmentManager().getAttachmentInfo(projectId, attachmentGroup, attachment).getLastModified());
		
		return params;
	}

	public static String authorizeGroup(String attachmentUrl) {
		try {
			URIBuilder builder = new URIBuilder(attachmentUrl);
			if (builder.getPathSegments().size() >= 5) {
				String group = builder.getPathSegments().get(4);
				byte[] encrypted = CryptoUtils.encrypt(group.getBytes(StandardCharsets.UTF_8));
				String base64 = Base64.encodeBase64URLSafeString(encrypted);
				builder.addParameter(PARAM_AUTHORIZATION, base64);
			}
			return builder.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
