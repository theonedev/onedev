package com.turbodev.server.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthorizedException;

import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.manager.VerificationManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.util.Verification;

@Path("/verifications")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class VerificationResource {

	private final VerificationManager verificationManager;
	
	private final ProjectManager projectManager;
	
	@Inject
	public VerificationResource(VerificationManager verificationManager, ProjectManager projectManager) {
		this.verificationManager = verificationManager;
		this.projectManager = projectManager;
	}
	
	@Path("/{projectId}/{commit}")
    @GET
    public Map<String, Verification> get(@PathParam("projectId") Long projectId, @PathParam("commit") String commit) {
		Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canRead(project))
    		throw new UnauthorizedException();
		
    	return verificationManager.getVerifications(project, commit);
    }
	
	@Path("/{projectId}/{commit}/{name}")
    @GET
    public Verification get(@PathParam("projectId") Long projectId, @PathParam("commit") String commit, 
    		@PathParam("name") String name) {
		Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canRead(project))
    		throw new UnauthorizedException();
		
    	return verificationManager.getVerifications(project, commit).get(name);
    }
	
	@Path("/{projectId}/{commit}/{name}")
    @POST
    public void save(@PathParam("projectId") Long projectId, @PathParam("commit") String commit, 
    		@PathParam("name") String name, @NotNull @Valid Verification verification) {
		Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canWrite(project))
    		throw new UnauthorizedException();
		
    	verificationManager.saveVerification(project, commit, name, verification);
    }
    
}
