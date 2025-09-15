package io.onedev.server.rest.resource;

import java.io.Serializable;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.onedev.server.OneDev;
import io.onedev.server.rest.annotation.Api;

@Api(description = "Version info for server and various tools")
@Path("/version")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class VersionResource {
	
	@Api(order=100, description = "Get server version")
	@Path("/server")
	@GET
	public String getServerVersion() {
		return OneDev.getInstance().getVersion();
	}
	
	@Api(description = "Get tod version range compatible with this server")
	@Path("/compatible-tod-versions")
	@GET
	public VersionRange getCompatibleTodVersions() {
		var versionRange = new VersionRange();
		versionRange.minVersion = "2.0.0";
		return versionRange;
	}
	
	private static class VersionRange implements Serializable {
		
		@Api(example = "1.0.0", description = "Minimum version. Null means no lower bound")
		String minVersion;

		@Api(example = "2.0.0", description = "Maximum version. Null means no upper bound")
		String maxVersion;

	}
	
}
