package com.gitplex.server.web.util.resource;

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
import org.eclipse.jgit.archive.TgzFormat;
import org.eclipse.jgit.archive.ZipFormat;
import org.eclipse.jgit.lib.Constants;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.security.SecurityUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class ArchiveResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_ACCOUNT = "account";
	
	private static final String PARAM_DEPOT = "depot";
	
	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_FORMAT = "format";
	
	public static final String FORMAT_ZIP = "zip";
	
	public static final String FORMAT_TGZ = "tgz";
	
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
		
		Depot depot = GitPlex.getInstance(DepotManager.class).find(userName, repoName);
		
		if (depot == null) 
			throw new EntityNotFoundException("Unable to find repository " + userName + "/" + repoName);
		
		String revision = params.get(PARAM_REVISION).toString();
		if (StringUtils.isBlank(revision))
			throw new IllegalArgumentException("revision parameter has to be specified");
		
		String format = params.get(PARAM_FORMAT).toString();
		if (!FORMAT_ZIP.equals(format) && !FORMAT_TGZ.equals(format)) {
			throw new IllegalArgumentException("format parameter should be specified either zip or tar.gz");
		}
		
		if (!SecurityUtils.canRead(depot)) 
			throw new UnauthorizedException();

		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			String fileName;
			if (FORMAT_ZIP.equals(format))
				fileName = revision + ".zip";
			else
				fileName = revision + ".tar.gz";
			response.setFileName(URLEncoder.encode(fileName, Charsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				if (format.equals("zip"))
					ArchiveCommand.registerFormat(format, new ZipFormat());
				else
					ArchiveCommand.registerFormat(format, new TgzFormat());
				try {
					ArchiveCommand archive = Git.wrap(depot.getRepository()).archive();
					archive.setFormat(format);
					archive.setTree(depot.getRevCommit(revision).getId());
					archive.setOutputStream(attributes.getResponse().getOutputStream());
					archive.call();
				} catch (GitAPIException e) {
					throw new RuntimeException(e);
				} finally {
					ArchiveCommand.unregisterFormat(format);
				}
			}				
		});

		return response;
	}

	public static PageParameters paramsOf(Depot depot, String revision, String format) {
		PageParameters params = new PageParameters();
		params.add(PARAM_ACCOUNT, depot.getAccount().getName());
		params.set(PARAM_DEPOT, depot.getName());
		params.set(PARAM_REVISION, revision);
		params.set(PARAM_FORMAT, format);
		
		return params;
	}
	
}
