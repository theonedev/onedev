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
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.security.SecurityUtils;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_USER = "user";
	
	private static final String PARAM_DEPOT = "depot";
	
	private static final String PARAM_REQUEST = "request";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String userName = params.get(PARAM_USER).toString();
		if (StringUtils.isBlank(userName))
			throw new IllegalArgumentException("account name has to be specified");
		
		String repoName = Preconditions.checkNotNull(params.get(PARAM_DEPOT).toString());
		if (StringUtils.isBlank(repoName))
			throw new IllegalArgumentException("repository name has to be specified");
		
		if (repoName.endsWith(Constants.DOT_GIT_EXT))
			repoName = repoName.substring(0, repoName.length() - Constants.DOT_GIT_EXT.length());
		
		final Depot depot = GitPlex.getInstance(DepotManager.class).findBy(userName, repoName);
		
		if (depot == null) 
			throw new EntityNotFoundException("Unable to find repository " + userName + "/" + repoName);
		
		Long requestId = params.get(PARAM_REQUEST).toOptionalLong();
		if (requestId == null)
			throw new IllegalArgumentException("request parameter has to be specified");
		
		PullRequest request = GitPlex.getInstance(Dao.class).load(PullRequest.class, requestId);
		
		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");

		if (!SecurityUtils.canPull(depot)) 
			throw new UnauthorizedException();

		final File attachmentFile = new File(getAttachmentsDir(request), attachment);
		
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

	private static File getAttachmentsDir(PullRequest request) {
		return GitPlex.getInstance(StorageManager.class).getAttachmentsDir(request);		
	}
	
	public static PageParameters paramsOf(PullRequest request, String attachment) {
		PageParameters params = new PageParameters();
		params.add(PARAM_USER, request.getTargetDepot().getAccount().getName());
		params.set(PARAM_DEPOT, request.getTargetDepot().getName());
		params.set(PARAM_REQUEST, request.getId());
		params.set(PARAM_ATTACHMENT, attachment);
		final File attachmentFile = new File(getAttachmentsDir(request), attachment);
		params.set("v", attachmentFile.lastModified());
		
		return params;
	}
	
}
