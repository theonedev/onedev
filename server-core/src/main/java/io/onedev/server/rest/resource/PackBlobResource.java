package io.onedev.server.rest.resource;

import io.onedev.server.entitymanager.PackBlobManager;
import io.onedev.server.model.PackBlob;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Api(order=4400, name="Package Blob")
@Path("/package-blobs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PackBlobResource {
	
	private final PackBlobManager packBlobManager;
	
	@Inject
	public PackBlobResource(PackBlobManager packBlobManager) {
		this.packBlobManager = packBlobManager;
	}

	@Api(order=100, description = "Find package blob by hash")
	@GET
	public PackBlob findByHash(@QueryParam("hash") String hash) {
		var packBlob = packBlobManager.find(hash);
		if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) 
			return packBlob;			
		else 
			return null;			
	}
	
	@Api(order=100, description = "Download package blob")
	@Path("/{packBlobId}/content")
	@GET
	@Produces(APPLICATION_OCTET_STREAM)
	public StreamingOutput downloadBlob(@PathParam("packBlobId") Long packBlobId) {
		var packBlob = packBlobManager.load(packBlobId);
		if (!SecurityUtils.canReadPackBlob(packBlob))
			throw new UnauthorizedException();
		
		var projectId = packBlob.getProject().getId();
		var hash = packBlob.getHash();
		return os -> {
			packBlobManager.downloadBlob(projectId, hash, os);
		};
	}

}
