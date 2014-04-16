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
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.service.FileBlobService;
import com.pmease.gitop.web.service.FileTypes;

@Path("/blob")
public class BlobResource {

	@GET
	@Path("raw/{user}/{repository}/{objectId}/{path:.*}")
	public Response getRawContent(@PathParam("user") String username,
								  @PathParam("repository") String repositoryName,
								  final @PathParam("objectId") String revision,
								  final @PathParam("path") String path) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(repositoryName));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(revision));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
		
		final Repository repository = Gitop.getInstance(RepositoryManager.class).findBy(username, repositoryName);
		
		if (repository == null) {
			return Response.status(Status.NOT_FOUND)
				.entity("Repository " + username + "/" + repositoryName + " doesn't exist")
				.build();
		}
		
		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(repository))) {
			return Response.status(Status.FORBIDDEN)
					.entity("You have no permission to access this resource")
					.build();
		}
		
		FileBlob blob = Gitop.getInstance(FileBlobService.class).get(repository, revision, path);
		
		StreamingOutput stream = new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				ObjectStream os = Gitop.getInstance(FileBlobService.class).openStream(repository, revision, path);
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
