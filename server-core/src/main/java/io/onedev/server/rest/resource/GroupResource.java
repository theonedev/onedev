package io.onedev.server.rest.resource;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.GroupService;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Membership;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/groups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class GroupResource {

	private final GroupService groupService;
	
	private final AuditService auditService;
	
	@Inject
	public GroupResource(GroupService groupService, AuditService auditService) {
		this.groupService = groupService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{groupId}")
    @GET
    public Group getGroup(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return groupService.load(groupId);
    }

	@Api(order=200)
	@Path("/{groupId}/authorizations")
    @GET
    public Collection<GroupAuthorization> getAuthorizations(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return groupService.load(groupId).getAuthorizations();
    }
	
	@Api(order=300)
	@Path("/{groupId}/memberships")
    @GET
    public Collection<Membership> getMemberships(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return groupService.load(groupId).getMemberships();
    }
	
	@Api(order=400)
	@GET
    public List<Group> queryGroups(@QueryParam("name") String name, @QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		
    	return groupService.query(criteria, offset, count);
    }

	@Api(order=450)
	@Path("/ids/{name}")
	@GET
	public Long getGroupId(@PathParam("name") @Api(description = "Group name") String name) {
		var group = groupService.find(name);
		if (group != null)
			return group.getId();
		else
			throw new NotFoundException();
	}

	@Api(order=500, description="Create new group")
    @POST
    public Long createGroup(@NotNull Group group) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		groupService.create(group);
		var newAuditContent = VersionedXmlDoc.fromBean(group).toXML();
		auditService.audit(null, "created group \"" + group.getName() + "\" via RESTful API", null, newAuditContent);
    	return group.getId();
    }

	@Api(order=550, description="Update group of specified id")
	@Path("/{groupId}")
	@POST
	public Response updateGroup(@PathParam("groupId") Long groupId, @NotNull Group group) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var oldName = group.getOldVersion().getRootElement().elementText(Group.PROP_NAME);
		groupService.update(group, oldName);

		var oldAuditContent = group.getOldVersion().toXML();
		var newAuditContent = VersionedXmlDoc.fromBean(group).toXML();
		auditService.audit(null, "changed group \"" + group.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);

		return Response.ok().build();
	}
	
	@Api(order=600)
	@Path("/{groupId}")
    @DELETE
    public Response deleteGroup(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var group = groupService.load(groupId);
    	groupService.delete(group);
		var oldAuditContent = VersionedXmlDoc.fromBean(group).toXML();
		auditService.audit(null, "deleted group \"" + group.getName() + "\" via RESTful API", oldAuditContent, null);
    	return Response.ok().build();
    }
	
}
