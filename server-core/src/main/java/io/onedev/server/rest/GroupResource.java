package io.onedev.server.rest;

import java.util.Collection;
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

import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Membership;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.GroupFacade;

@Api(order=6000)
@Path("/groups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class GroupResource {

	private final GroupManager groupManager;
	
	@Inject
	public GroupResource(GroupManager groupManager) {
		this.groupManager = groupManager;
	}

	@Api(order=100)
	@Path("/{groupId}")
    @GET
    public Group getBasicInfo(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return groupManager.load(groupId);
    }

	@Api(order=200)
	@Path("/{groupId}/authorizations")
    @GET
    public Collection<GroupAuthorization> getAuthorizations(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return groupManager.load(groupId).getAuthorizations();
    }
	
	@Api(order=300)
	@Path("/{groupId}/memberships")
    @GET
    public Collection<Membership> getMemberships(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return groupManager.load(groupId).getMemberships();
    }
	
	@Api(order=400)
	@GET
    public List<Group> queryBasicInfo(@QueryParam("name") String name, @QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		
    	return groupManager.query(criteria, offset, count);
    }
	
	@Api(order=500, description="Create new group")
    @POST
    public Long create(@NotNull Group group) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		groupManager.create(group);
    		
    	return group.getId();
    }

	@Api(order=550, description="Update group of specified id")
	@Path("/{groupId}")
	@POST
	public Response update(@PathParam("groupId") Long groupId, @NotNull Group group) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		if (group.getOldVersion() != null)
			groupManager.update(group, ((GroupFacade) group.getOldVersion()).getName());
		else
			groupManager.update(group, null);
		return Response.ok().build();
	}
	
	@Api(order=600)
	@Path("/{groupId}")
    @DELETE
    public Response delete(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	groupManager.delete(groupManager.load(groupId));
    	return Response.ok().build();
    }
	
}
