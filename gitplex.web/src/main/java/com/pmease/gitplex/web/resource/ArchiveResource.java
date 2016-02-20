package com.pmease.gitplex.web.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jgit.api.ArchiveCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.archive.ZipFormat;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;

public class ArchiveResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_USER = "user";
	
	private static final String PARAM_DEPOT = "depot";
	
	private static final String PARAM_REVISION = "revision";
	
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
		
		final String revision = params.get(PARAM_REVISION).toString();
		if (StringUtils.isBlank(revision))
			throw new IllegalArgumentException("revision parameter has to be specified");
		
		if (!SecurityUtils.canPull(depot)) 
			throw new UnauthorizedException();

		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			response.setFileName(URLEncoder.encode(revision+".zip", Charsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				ArchiveCommand.registerFormat("zip", new ZipFormat());
				try (Repository repository = depot.openRepository()) {
					ArchiveCommand archive = Git.wrap(repository).archive();
					archive.setFormat("zip");
					archive.setTree(depot.getRevCommit(revision).getId());
					archive.setOutputStream(attributes.getResponse().getOutputStream());
					archive.call();
				} catch (GitAPIException e) {
					throw new RuntimeException(e);
				} finally {
					ArchiveCommand.unregisterFormat("zip");
				}
			}				
		});

		return response;
	}

	public static PageParameters paramsOf(Depot depot, String revision) {
		PageParameters params = new PageParameters();
		params.add(PARAM_USER, depot.getOwner().getName());
		params.set(PARAM_DEPOT, depot.getName());
		params.set(PARAM_REVISION, revision);
		
		return params;
	}
	
}
