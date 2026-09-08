package io.onedev.server.rest.resource;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.onedev.server.OneDev;
import io.onedev.server.rest.annotation.Api;

@Api(description = "Server readiness and version")
@Path("/server")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ServerResource {
	
	@Api(order=100, description = "Check whether server initialization is complete")
	@Path("/ready")
	@GET
	public boolean isReady() {
		return OneDev.getInstance().isReady();
	}

	@Api(order=200, description = "Get server version")
	@Path("/version")
	@GET
	public String getVersion() {
		return OneDev.getInstance().getVersion();
	}
	
}
