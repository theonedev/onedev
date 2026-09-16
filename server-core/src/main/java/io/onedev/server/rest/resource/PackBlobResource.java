package io.onedev.server.rest.resource;

import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.service.PackBlobService;
import io.onedev.server.model.PackBlob;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(name="Package Blob")
@Path("/package-blobs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PackBlobResource {
	
	private final PackBlobService packBlobService;
	
	@Inject
	public PackBlobResource(PackBlobService packBlobService) {
		this.packBlobService = packBlobService;
	}

	@Api(order=100, description = "Find package blob by project id and hash")
	@GET
	public PackBlob findByHash(@QueryParam("projectId") Long projectId, @QueryParam("hash") String hash) {
		var packBlob = packBlobService.findBySha256Hash(projectId, hash);
		if (packBlob != null) 
			return packBlob;			
		else 
			return null;			
	}
	
	@Api(order=100, description = "Download package blob")
	@Path("/{packBlobId}/content")
	@GET
	@Produces(APPLICATION_OCTET_STREAM)
	public StreamingOutput downloadBlob(@PathParam("packBlobId") Long packBlobId) {
		var packBlob = packBlobService.load(packBlobId);
		if (!SecurityUtils.canReadPack(packBlob.getProject()))
			throw new UnauthorizedException();
		
		var projectId = packBlob.getProject().getId();
		var hash = packBlob.getSha256Hash();
		return os -> {
			packBlobService.downloadBlob(projectId, hash, os);
		};
	}

}
