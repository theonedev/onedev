package com.gitplex.server.rest.github;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;

import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.VerificationManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.rest.RestConstants;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.Verification;
import com.google.common.base.Preconditions;

@Path("/repos/projects")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
@Singleton
public class CommitStatusResource {

	private final ProjectManager projectManager;
	
	private final VerificationManager verificationManager;
	
	@Inject
	public CommitStatusResource(ProjectManager projectManager, VerificationManager verificationManager) {
		this.projectManager = projectManager;
		this.verificationManager = verificationManager;
	}
	
	private Project getProject(String projectName) {
		return Preconditions.checkNotNull(projectManager.find(projectName));
	}
	
	@Path("/{projectName}/statuses/{commit}")
    @POST
    public Response save(@PathParam("projectName") String projectName, @PathParam("commit") String commit, 
    		@NotNull @Valid Map<String, String> commitStatus, @Context UriInfo uriInfo) {
		
		Project project = getProject(projectName);
    	if (!SecurityUtils.canWrite(project))
    		throw new UnauthorizedException();
    	
    	Verification verification = new Verification(
    			Verification.Status.valueOf(commitStatus.get("state").toUpperCase()), 
    			new Date(), commitStatus.get("description"), commitStatus.get("target_url"));
    	String context = commitStatus.get("context");
    	if (context == null)
    		context = "default";
    	verificationManager.saveVerification(project, commit, context, verification);
    	UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
    	uriBuilder.path(context);
    	commitStatus.put("id", "1");
    	
    	return Response.created(uriBuilder.build()).entity(commitStatus).type(RestConstants.JSON_UTF8).build();
    }
	
}
