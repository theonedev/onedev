package io.onedev.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.security.SecurityUtils;

@Api(order=7000)
@Path("/roles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RoleResource {

	private final RoleManager roleManager;
	
	@Inject
	public RoleResource(RoleManager roleManager) {
		this.roleManager = roleManager;
	}

	@Api(order=100)
	@Path("/{roleId}")
    @GET
    public Role get(@PathParam("roleId") Long roleId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return roleManager.load(roleId);
    }

	@Api(order=200)
	@GET
    public List<Role> query(@QueryParam("name") String name, @QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should be less than " + RestConstants.MAX_PAGE_SIZE);

		EntityCriteria<Role> criteria = EntityCriteria.of(Role.class);
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		
    	return roleManager.query(criteria, offset, count);
    }
	
	@Api(order=300, description="Update role of specified id in request body, or create new if id property not provided")
    @POST
    public Long createOrUpdate(@NotNull Role role) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
	    roleManager.save(role, (String) role.getCustomData());
    	return role.getId();
    }
	
	@Api(order=400)
	@Path("/{roleId}")
    @DELETE
    public Response delete(@PathParam("roleId") Long roleId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	roleManager.delete(roleManager.load(roleId));
    	return Response.ok().build();
    }
	
}
