package com.pmease.gitop.web.resource;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.shiro.SecurityUtils;
import org.eclipse.jgit.lib.ObjectStream;
import org.parboiled.common.Preconditions;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.service.FileBlobService;
import com.pmease.gitop.web.service.FileTypes;

@Path("/blob")
public class BlobResource {

	@GET
	@Path("raw/{user}/{project}/{objectId}/{path:.*}")
	public Response getRawContent(@PathParam("user") String username,
								  @PathParam("project") String projectName,
								  final @PathParam("objectId") String revision,
								  final @PathParam("path") String path) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(projectName));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(revision));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
		
		final Project project = Gitop.getInstance(ProjectManager.class).findBy(username, projectName);
		
		if (project == null) {
			return Response.status(Status.NOT_FOUND)
				.entity("Project " + username + "/" + projectName + " doesn't exist")
				.build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(project))) {
			return Response.status(Status.FORBIDDEN)
					.entity("You have no permission to access this resource")
					.build();
		}
		
		FileBlob blob = Gitop.getInstance(FileBlobService.class).get(project, revision, path);
		
		StreamingOutput stream = new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				ObjectStream os = Gitop.getInstance(FileBlobService.class).openStream(project, revision, path);
				ByteStreams.copy(os, output);
			}
		};
		
		String disposition = Gitop.getInstance(FileTypes.class).isSafeInline(blob.getMediaType()) ?
				"inline" : "attachment; filename = " + blob.getName();
		
		return Response.ok(stream, blob.getMediaType().toString())
				.header("content-disposition", disposition)
				.build();
	}
}
