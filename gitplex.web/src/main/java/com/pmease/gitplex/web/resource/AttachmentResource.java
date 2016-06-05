package com.pmease.gitplex.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.io.IOUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.AttachmentManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.security.SecurityUtils;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_ACCOUNT = "account";
	
	private static final String PARAM_DEPOT = "depot";
	
	private static final String PARAM_UUID = "uuid";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String userName = params.get(PARAM_ACCOUNT).toString();
		if (StringUtils.isBlank(userName))
			throw new IllegalArgumentException("account name has to be specified");
		
		String repoName = Preconditions.checkNotNull(params.get(PARAM_DEPOT).toString());
		if (StringUtils.isBlank(repoName))
			throw new IllegalArgumentException("repository name has to be specified");
		
		if (repoName.endsWith(Constants.DOT_GIT_EXT))
			repoName = repoName.substring(0, repoName.length() - Constants.DOT_GIT_EXT.length());
		
		Depot depot = GitPlex.getInstance(DepotManager.class).findBy(userName, repoName);
		
		if (depot == null) 
			throw new EntityNotFoundException("Unable to find repository " + userName + "/" + repoName);
		
		if (!SecurityUtils.canRead(depot)) 
			throw new UnauthorizedException();

		String storage = params.get(PARAM_UUID).toString();
		if (StringUtils.isBlank(storage))
			throw new IllegalArgumentException("uuid parameter has to be specified");

		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");

		File attachmentFile = new File(getAttachmentDir(depot, storage), attachment);
		
		ResourceResponse response = new ResourceResponse();
		response.setContentLength(attachmentFile.length());
		try {
			response.setContentType(Files.probeContentType(attachmentFile.toPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			response.setFileName(URLEncoder.encode(attachment, Charsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
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

	private static File getAttachmentDir(Depot depot, String uuid) {
		return GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(depot, uuid);		
	}
	
	public static PageParameters paramsOf(Depot depot, String attachmentDirUUID, String attachmentName) {
		PageParameters params = new PageParameters();
		params.add(PARAM_ACCOUNT, depot.getAccount().getName());
		params.set(PARAM_DEPOT, depot.getName());
		params.set(PARAM_UUID, attachmentDirUUID);
		params.set(PARAM_ATTACHMENT, attachmentName);
		final File attachmentFile = new File(getAttachmentDir(depot, attachmentDirUUID), attachmentName);
		params.set("v", attachmentFile.lastModified());
		
		return params;
	}
	
}
