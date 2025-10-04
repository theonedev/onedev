package io.onedev.server.rest.resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.RoleService;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.security.SecurityUtils;

@Path("/roles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RoleResource {

	private final RoleService roleService;
	
	private final AuditService auditService;
	
	@Inject
	public RoleResource(RoleService roleService, AuditService auditService) {
		this.roleService = roleService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{roleId}")
    @GET
    public Role getRole(@PathParam("roleId") Long roleId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return roleService.load(roleId);
    }	

	@Api(order=200)
	@GET
    public List<Role> queryRoles(@QueryParam("name") String name, @QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

		EntityCriteria<Role> criteria = EntityCriteria.of(Role.class);
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		
    	return roleService.query(name, offset, count);
    }

	@Api(order=250)
	@Path("/ids/{name}")
	@GET
	public Long getRoleId(@PathParam("name") String name) {
		if (SecurityUtils.getAuthUser() == null)
			throw new UnauthenticatedException();

		var role = roleService.find(name);
		if (role != null)
			return role.getId();
		else
			throw new NotFoundException();
	}

	@Api(order=300, description="Create new role")
    @POST
    public Long createRole(@NotNull Role role) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		roleService.create(role, null);
		var auditContent = VersionedXmlDoc.fromBean(role).toXML();
		auditService.audit(null, "created role \"" + role.getName() + "\" via RESTful API", null, auditContent);

    	return role.getId();
    }

	@Api(order=350, description="Update role of specified id")
	@Path("/{roleId}")
	@POST
	public Response updateRole(@PathParam("roleId") Long roleId, @NotNull Role role) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

		var oldAuditContent = role.getOldVersion().toXML();
		var newAuditContent = VersionedXmlDoc.fromBean(role).toXML();

		var oldName = role.getOldVersion().getRootElement().elementText(Role.PROP_NAME);
		roleService.update(role, null, oldName);

		auditService.audit(null, "changed role \"" + role.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);

		return Response.ok().build();
	}
	
	@Api(order=400)
	@Path("/{roleId}")
    @DELETE
    public Response deleteRole(@PathParam("roleId") Long roleId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var role = roleService.load(roleId);
		var oldAuditContent = VersionedXmlDoc.fromBean(role).toXML();
    	roleService.delete(role);
		auditService.audit(null, "deleted role \"" + role.getName() + "\" via RESTful API", oldAuditContent, null);
    	return Response.ok().build();
    }	

}
