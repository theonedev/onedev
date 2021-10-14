package io.onedev.server.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.io.IOUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.AttachmentStorageManager;
import io.onedev.server.util.CryptoUtils;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_GROUP = "group";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	public static final String PARAM_AUTHORIZATION = "authorization";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		
		Long projectId = params.get(PARAM_PROJECT).toLong();
		Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
		
		String group = params.get(PARAM_GROUP).toString();
		if (StringUtils.isBlank(group))
			throw new IllegalArgumentException("group parameter has to be specified");
		
		String authorization = params.get(PARAM_AUTHORIZATION).toOptionalString();
		if (authorization == null 
				|| !new String(CryptoUtils.decrypt(Base64.decodeBase64(authorization)), StandardCharsets.UTF_8).equals(group)) {
			PullRequest request = OneDev.getInstance(PullRequestManager.class).findByUUID(group);
			if (request != null && !SecurityUtils.canReadCode(project))
				throw new UnauthorizedException();
			else if (!SecurityUtils.canAccess(project))
				throw new UnauthorizedException();
		}

		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");

		File attachmentFile = new File(getGroupDir(project, group), attachment);
		if (!attachmentFile.exists()) 
			throw new RuntimeException("Attachment not found: " + attachment);
		
		ResourceResponse response = new ResourceResponse();
		response.setContentLength(attachmentFile.length());
		try {
			response.setContentType(Files.probeContentType(attachmentFile.toPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		response.setFileName(attachment);
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				try (InputStream is = new FileInputStream(attachmentFile);) {
					IOUtils.copy(is, attributes.getResponse().getOutputStream());
				}
			}
			
		});

		return response;
	}

	private static File getGroupDir(Project project, String group) {
		return OneDev.getInstance(AttachmentStorageManager.class).getGroupDir(project, group);		
	}
	
	public static PageParameters paramsOf(Project project, String group, String attachment) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getId());
		params.set(PARAM_GROUP, group);
		params.set(PARAM_ATTACHMENT, attachment);
		
		File attachmentFile = new File(getGroupDir(project, group), attachment);
		params.set("v", attachmentFile.lastModified());
		
		return params;
	}

	public static String authorizeGroup(String attachmentUrl) {
		try {
			URIBuilder builder = new URIBuilder(attachmentUrl);
			if (builder.getPathSegments().size() >= 4) {
				String group = builder.getPathSegments().get(3);
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
