package io.onedev.server.rest.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.ok;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.shiro.authz.UnauthorizedException;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.util.artifact.ArtifactInfo;

@Api(description="In most cases, artifact resource is operated with build id, which is different from build number. "
		+ "To get build id of a particular build number, use the <a href='/~help/api/io.onedev.server.rest.BuildResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;path/to/project#100&quot;</code> or <code>&quot;Number&quot; is &quot;PROJECTKEY-100&quot;</code>")
@Path("/artifacts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ArtifactResource {
	
	private final BuildService buildService;
	
	@Inject
	public ArtifactResource(BuildService buildService) {
		this.buildService = buildService;
	}
	
	@Nullable
	private String normalizeArtifactPath(@Nullable String artifactPath) {
		if (StringUtils.isNotBlank(artifactPath)) {
			artifactPath = StringUtils.stripStart(artifactPath, "/");
			if (StringUtils.isNotBlank(artifactPath)) {
				if (artifactPath.contains(".."))
					throw new ExplicitException("Invalid artifact path");
				return artifactPath;
			}
		} 
		return null;
	}
	
	@Api(order=100, description = "Get artifact info of specified path")
	@Path("/{buildId}/infos{artifactPath:(/.*)?}")
    @GET
    public ArtifactInfo getArtifactInfo(@PathParam("buildId") Long buildId, 
										@PathParam("artifactPath") @Api(example = "/path/to/directoryOrFile") String artifactPath) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canAccessProject(build.getProject()))
			throw new UnauthorizedException();
		return buildService.getArtifactInfo(build, normalizeArtifactPath(artifactPath));
    }

	@Api(order=200, description = "Download artifact of specified path")
	@Path("/{buildId}/contents/{artifactPath:(.*)}")
	@GET
	@Produces(APPLICATION_OCTET_STREAM)
	public StreamingOutput downloadArtifact(@PathParam("buildId") Long buildId,
									 @PathParam("artifactPath") @Api(example = "path/to/file") String artifactPath) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canAccessProject(build.getProject()))
			throw new UnauthorizedException();

		var projectId = build.getProject().getId();
		var buildNumber = build.getNumber();
		var normalizedPath = normalizeArtifactPath(artifactPath);
		return os -> {
			buildService.downloadArtifact(projectId, buildNumber, normalizedPath, os);
		};
	}

	@Api(order=300, description = "Upload artifact to specified path")
	@Path("/{buildId}/{artifactPath:(.*)}")
	@POST
	@Consumes(APPLICATION_OCTET_STREAM)
	public Response uploadArtifact(
			@PathParam("buildId") Long buildId, 
			@PathParam("artifactPath") @Api(example = "path/to/file") String artifactPath, 
			InputStream input) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canManageBuild(build))
			throw new UnauthorizedException();

		buildService.uploadArtifact(build.getProject().getId(), build.getNumber(),
				normalizeArtifactPath(artifactPath), input);
		return ok().build();
	}
	
	@Api(order=400, description = "Delete artifact of specified path, or delete all artifacts " +
			"if artifact path is not specified")
	@Path("/{buildId}{artifactPath:(/.*)?}")
	@DELETE
	public Response deleteArtifact(
			@PathParam("buildId") Long buildId, 
			@PathParam("artifactPath") @Api(example = "/path/to/directoryOrFile") String artifactPath) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canManageBuild(build))
			throw new UnauthorizedException();
		
		buildService.deleteArtifact(build, normalizeArtifactPath(artifactPath));
		return ok().build();
	}
	
}
