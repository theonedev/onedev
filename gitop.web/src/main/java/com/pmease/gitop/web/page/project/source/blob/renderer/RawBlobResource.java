package com.pmease.gitop.web.page.project.source.blob.renderer;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.tika.io.IOUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.eclipse.jgit.lib.ObjectStream;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.project.source.blob.FileBlob;

public class RawBlobResource extends AbstractResource {
	private static final long serialVersionUID = 1L;

	public RawBlobResource() {
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		Long projectId = params.get("project").toLongObject();
		String revision = params.get("objectId").toString();
		String path = params.get("path").toString();
		
		Preconditions.checkArgument(projectId != null);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(revision));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
		
		Project project = Gitop.getInstance(ProjectManager.class).get(projectId);
		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(project))) {
			throw new AccessDeniedException("User " + SecurityUtils.getSubject() 
					+ " have no permission to access project " 
					+ project.getPathName());
		}
		
		ResourceResponse response = new ResourceResponse();
		
		if (response.dataNeedsToBeWritten(attributes)) {
			final FileBlob blob = FileBlob.of(project, revision, path);
			response.setContentLength(blob.getSize());
			String fileName = FilenameUtils.getName(blob.getPath());
			response.setFileName(fileName);
			response.setContentType(blob.getMimeType().getType().toString());
			response.setContentDisposition(ContentDisposition.ATTACHMENT);
			
			response.setWriteCallback(new WriteCallback() {
				@Override
				public void writeData(final Attributes attributes) {
					ObjectStream os = blob.openStream();
					try {
						ByteStreams.copy(os, attributes.getResponse().getOutputStream());
					} catch (IOException e) {
						throw Throwables.propagate(e);
					} finally {
						IOUtils.closeQuietly(os);
					}
				}
			});
		}
		
		return response;
	}

	
}
