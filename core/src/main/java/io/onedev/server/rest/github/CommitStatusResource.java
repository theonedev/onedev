package io.onedev.server.rest.github;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.exception.OneException;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.rest.RestConstants;
import io.onedev.server.security.SecurityUtils;
import io.onedev.utils.StringUtils;

@Path("/repos/projects")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
@Singleton
public class CommitStatusResource {

	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
	private final ConfigurationManager configurationManager;
	
	private final ObjectMapper objectMapper;
	
	@Inject
	public CommitStatusResource(ProjectManager projectManager, BuildManager buildManager, 
			ConfigurationManager configurationManager, ObjectMapper objectMapper) {
		this.projectManager = projectManager;
		this.buildManager = buildManager;
		this.objectMapper = objectMapper;
		this.configurationManager = configurationManager;
	}
	
	private Project getProject(String projectName) {
		Project project = projectManager.find(projectName);
		if (project == null)
			throw new OneException("Unable to find project: " + projectName);
		return project;
	}
	
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{projectName}/statuses/{commit}")
    @POST
    public Response save(@PathParam("projectName") String projectName, @PathParam("commit") String commit, 
    		Map<String, String> commitStatus, @Context UriInfo uriInfo) {

		Project project = getProject(projectName);
    	if (!SecurityUtils.canWriteCode(project.getFacade()))
    		throw new UnauthorizedException();
    	
    	String context = commitStatus.get("context");
    	if (context == null)
    		context = "default";
    	
    	Configuration configuration = configurationManager.find(project, context);
    	if (configuration == null) {
    		String message = String.format("Unable to find configuration (project: %s, name: %s)", project.getName(), context);
    		throw new OneException(message);
    	}
    	
    	String status = commitStatus.get("state").toUpperCase();
    	if (status.equals("PENDING"))
    		status = "RUNNING";
    	Build build = buildManager.findByCommit(configuration, commit);
    	if (build == null) {
    		build = new Build();
    		build.setConfiguration(configuration);
        	build.setCommit(commit);
    	}
    	build.setName(StringUtils.substringBefore(commitStatus.get("description"), ":"));
    	build.setStatus(Build.Status.valueOf(status));
    	build.setDate(new Date());
    	build.setUrl(commitStatus.get("target_url"));
    	buildManager.save(build);
    	UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
    	uriBuilder.path(context);
    	commitStatus.put("id", build.getId().toString());
    	
    	return Response.created(uriBuilder.build()).entity(commitStatus).type(RestConstants.JSON_UTF8).build();
    }

	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/{projectName}/statuses/{commit}")
    @POST
    public Response save(@PathParam("projectName") String projectName, @PathParam("commit") String commit, 
    		MultivaluedMap<String, String> map, @Context UriInfo uriInfo) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> commitStatus = objectMapper.readValue(map.keySet().iterator().next(), Map.class);
			return save(projectName, commit, commitStatus, uriInfo);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
	
}
