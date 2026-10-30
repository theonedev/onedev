package io.onedev.server.rest.resource;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.server.persistence.dao.MatchMode;
import io.onedev.server.persistence.dao.Restrictions;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.GroupService;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Membership;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
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
    public GroupData getGroup(@PathParam("groupId") Long groupId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		return GroupData.from(groupService.load(groupId));
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
	public List<GroupData> queryGroups(@QueryParam("name") String name, @QueryParam("offset") @Api(example="0") int offset,
    		@QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		
		return groupService.query(criteria, offset, count).stream().map(GroupData::from).toList();
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
    public Long createGroup(@NotNull @Valid GroupData data) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		var group = new Group();
		data.populate(group);
		groupService.create(group);
		var newAuditContent = VersionedXmlDoc.fromBean(GroupData.from(group)).toXML();
		auditService.audit(null, "created group \"" + group.getName() + "\" via RESTful API", null, newAuditContent);
    	return group.getId();
    }

	@Api(order=550, description="Update group of specified id. Supply all group fields; omitted optional fields are reset to their defaults")
	@Path("/{groupId}")
	@POST
	public Response updateGroup(@PathParam("groupId") Long groupId, @NotNull @Valid GroupData data) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var group = groupService.load(groupId);
		var oldName = group.getName();
		var oldAuditContent = VersionedXmlDoc.fromBean(GroupData.from(group)).toXML();
		data.populate(group);
		groupService.update(group, oldName);

		var newAuditContent = VersionedXmlDoc.fromBean(GroupData.from(group)).toXML();
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
		var oldAuditContent = VersionedXmlDoc.fromBean(GroupData.from(group)).toXML();
		auditService.audit(null, "deleted group \"" + group.getName() + "\" via RESTful API", oldAuditContent, null);
    	return Response.ok().build();
    }

	@EntityCreate(Group.class)
	public static class GroupData implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		@Api(order=50)
		@Id
		@JsonProperty(access = JsonProperty.Access.READ_ONLY)
		private Long id;

		@Api(order=100)
		private String name;

		@Api(order=200, description="May be empty")
		private String description;

		@Api(order=300)
		private boolean administrator;

		@Api(order=350)
		private boolean createRootProjects;

		@Api(order=400)
		private boolean enforce2FA;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isAdministrator() {
			return administrator;
		}

		public void setAdministrator(boolean administrator) {
			this.administrator = administrator;
		}

		public boolean isCreateRootProjects() {
			return createRootProjects;
		}

		public void setCreateRootProjects(boolean createRootProjects) {
			this.createRootProjects = createRootProjects;
		}

		public boolean isEnforce2FA() {
			return enforce2FA;
		}

		public void setEnforce2FA(boolean enforce2FA) {
			this.enforce2FA = enforce2FA;
		}

		public void populate(Group group) {
			group.setName(getName());
			group.setDescription(getDescription());
			group.setAdministrator(isAdministrator());
			group.setCreateRootProjects(isCreateRootProjects());
			group.setEnforce2FA(isEnforce2FA());
		}

		public static GroupData from(Group group) {
			var data = new GroupData();
			data.setId(group.getId());
			data.setName(group.getName());
			data.setDescription(group.getDescription());
			data.setAdministrator(group.isAdministrator());
			data.setCreateRootProjects(group.isCreateRootProjects());
			data.setEnforce2FA(group.isEnforce2FA());
			return data;
		}

	}

}
