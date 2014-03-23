package com.pmease.gitop.web.page.project.source.tags;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.git.command.ArchiveCommand;
import com.pmease.gitop.web.git.command.ArchiveCommand.Format;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.service.FileTypes;

@SuppressWarnings("serial")
public class GitArchiveResource extends AbstractResource {

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		
		final String username = params.get(PageSpec.USER).toString();
		final String repoName = params.get(PageSpec.REPO).toString();
		final String fileName = params.get("file").toString();

		Preconditions.checkArgument(username != null);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(repoName));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName) && 
				(fileName.endsWith(".zip") || fileName.endsWith(".tar.gz")));

		Repository project = Gitop.getInstance(RepositoryManager.class).findBy(username, repoName);
		if (project == null) {
			throw new EntityNotFoundException("Repository " + username + "/" + repoName + " doesn't exist");
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(project))) {
			throw new AccessDeniedException("User " + SecurityUtils.getSubject() 
					+ " have no permission to access repository " 
					+ project.getPathName());
		}
		
		ResourceResponse response = new ResourceResponse();
		response.setFileName(fileName);
		
		MediaType mt = Gitop.getInstance(FileTypes.class).getMediaType(fileName, new byte[0]);
		response.setContentType(mt.toString());
		response.setContentDisposition(ContentDisposition.ATTACHMENT);
		final Long projectId = project.getId();
		response.setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(final Attributes attributes) {
				Repository project = Gitop.getInstance(RepositoryManager.class).get(projectId);
				ArchiveCommand ac = new ArchiveCommand(project.code().repoDir(),
						attributes.getResponse().getOutputStream());
				
				if (fileName.endsWith(".zip")) {
					String treeish = fileName.substring(0, fileName.length() - ".zip".length());
					ac.treeish(treeish).format(Format.ZIP);
				} else {
					// end with .tar.gz
					String treeish = fileName.substring(0, fileName.length() - ".tar.gz".length());
					ac.treeish(treeish).format(Format.TAR_GZ);
				}
				
				ac.call();
			}
		});
		
		return response;
	}

}
