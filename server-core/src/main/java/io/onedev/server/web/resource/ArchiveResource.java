package io.onedev.server.web.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
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

		String projectName = params.get(PARAM_PROJECT).toString();
		if (StringUtils.isBlank(projectName))
			throw new IllegalArgumentException("project name has to be specified");
		
		Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
		
		if (project == null) 
			throw new EntityNotFoundException("Unable to find project: " + projectName);
		
		String revision = params.get(PARAM_REVISION).toString();
		if (StringUtils.isBlank(revision))
			throw new IllegalArgumentException("revision parameter has to be specified");
		
		String format = params.get(PARAM_FORMAT).toString();
		if (!FORMAT_ZIP.equals(format) && !FORMAT_TGZ.equals(format)) {
			throw new IllegalArgumentException("format parameter should be specified either zip or tar.gz");
		}
		
		if (!SecurityUtils.canReadCode(project)) 
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
			response.setFileName(URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
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
					ArchiveCommand archive = Git.wrap(project.getRepository()).archive();
					archive.setFormat(format);
					archive.setTree(project.getRevCommit(revision, true).getId());
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

	public static PageParameters paramsOf(Project project, String revision, String format) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getName());
		params.set(PARAM_REVISION, revision);
		params.set(PARAM_FORMAT, format);
		
		return params;
	}
	
}
