package io.onedev.server.rest.resource;

import io.onedev.server.OneDev;
import io.onedev.server.rest.annotation.Api;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;

@Api(order=100000, description = "Version info for server and various tools")
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
		return new VersionRange();
	}
	
	private static class VersionRange implements Serializable {
		
		@Api(example = "1.0.0", description = "Minimum version. Null means no lower bound")
		String minVersion;

		@Api(example = "2.0.0", description = "Maximum version. Null means no upper bound")
		String maxVersion;

		public String getMinVersion() {
			return minVersion;
		}

		public void setMinVersion(String minVersion) {
			this.minVersion = minVersion;
		}

		public String getMaxVersion() {
			return maxVersion;
		}

		public void setMaxVersion(String maxVersion) {
			this.maxVersion = maxVersion;
		}
	}
	
}
