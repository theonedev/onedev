package com.turbodev.server.rest.github;

import java.util.ArrayList;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.turbodev.server.rest.RestConstants;

@Path("/repos/projects")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
@Singleton
public class IssueCommentResource {

	@Path("/{projectName}/issues/{issueNumber}/comments")
    @POST
    public Response save(@PathParam("projectName") String projectName, 
    		@PathParam("issueNumber") Long issueNumber, @Context UriInfo uriInfo) {
    	return Response.created(uriInfo.getAbsolutePath()).build();
    }
	
	@Path("/{projectName}/issues/{issueNumber}/comments")
    @GET
    public Response list(@PathParam("projectName") String projectName, 
    		@PathParam("issueNumber") Long issueNumber, @Context UriInfo uriInfo) {
    	return Response.ok(new ArrayList<>(), RestConstants.JSON_UTF8).build();
    }
	
}
