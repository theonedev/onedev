package io.onedev.server.rest;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.model.Configuration;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.jersey.ValidQueryParams;
import io.onedev.server.security.SecurityUtils;

@Path("/configurations")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ConfigurationResource {

	private final ConfigurationManager configurationManager;
	
	@Inject
	public ConfigurationResource(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}
	
	@ValidQueryParams
	@GET
    public Response query(@QueryParam("project") Long projectId, @QueryParam("name") String name, 
    		@QueryParam("offset") Integer offset, @QueryParam("count") Integer count, @Context UriInfo uriInfo) {
		EntityCriteria<Configuration> criteria = configurationManager.newCriteria();
		if (projectId != null)
			criteria.add(Restrictions.eq("project.id", projectId));
		if (name != null)
			criteria.add(Restrictions.eq("name", name));
		
    	if (offset == null)
    		offset = 0;
    	
    	if (count == null || count > RestConstants.PAGE_SIZE) 
    		count = RestConstants.PAGE_SIZE;

    	Collection<Configuration> configurations = configurationManager.query(criteria, offset, count);
		for (Configuration configuration: configurations) {
			if (!SecurityUtils.canReadIssues(configuration.getProject().getFacade()))
				throw new UnauthorizedException("Unable to access project '" + configuration.getProject().getName() + "'");
		}
		
		return Response.ok(configurations, RestConstants.JSON_UTF8).build();
		
    }
    
	@Path("/{configurationId}")
    @GET
    public Configuration get(@PathParam("configurationId") Long configurationId) {
    	Configuration configuration = configurationManager.load(configurationId);
    	if (!SecurityUtils.canReadIssues(configuration.getProject().getFacade()))
			throw new UnauthorizedException("Unauthorized access to project " + configuration.getProject().getName());
    	else
    		return configuration;
    }
	
}
