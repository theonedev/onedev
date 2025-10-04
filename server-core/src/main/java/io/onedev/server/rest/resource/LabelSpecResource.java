package io.onedev.server.rest.resource;

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

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/label-specs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class LabelSpecResource {

	private final LabelSpecService labelSpecService;
	
	private final AuditService auditService;
	
	@Inject
	public LabelSpecResource(LabelSpecService labelSpecService, AuditService auditService) {
		this.labelSpecService = labelSpecService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{labelSpecId}")
    @GET
    public LabelSpec getSpec(@PathParam("labelSpecId") Long labelSpecId) {
		if (SecurityUtils.getAuthUser() == null)
			throw new UnauthenticatedException();
		
    	return labelSpecService.load(labelSpecId);
    }
	
	@Api(order=400)
	@GET
    public List<LabelSpec> querySpecs(@QueryParam("name") String name, 
								 @QueryParam("offset") @Api(example="0") int offset, 
								 @QueryParam("count") @Api(example="100") int count) {
		if (SecurityUtils.getAuthUser() == null)
			throw new UnauthenticatedException();
						
		EntityCriteria<LabelSpec> criteria = EntityCriteria.of(LabelSpec.class);
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		
    	return labelSpecService.query(criteria, offset, count);
    }
	
	@Api(order=500, description="Create new label spec")
    @POST
    public Long createSpec(@NotNull LabelSpec labelSpec) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		labelSpecService.createOrUpdate(labelSpec);
		var newAuditContent = VersionedXmlDoc.fromBean(labelSpec).toXML();
		auditService.audit(null, "created label spec \"" + labelSpec.getName() + "\" via RESTful API", null, newAuditContent);
    	return labelSpec.getId();
    }

	@Api(order=550, description="Update label spec of specified id")
	@Path("/{labelSpecId}")
	@POST
	public Response updateSpec(@PathParam("labelSpecId") Long labelSpecId, @NotNull LabelSpec labelSpec) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		labelSpecService.createOrUpdate(labelSpec);
		var oldAuditContent = labelSpec.getOldVersion().toXML();
		var newAuditContent = VersionedXmlDoc.fromBean(labelSpec).toXML();
		auditService.audit(null, "changed label spec \"" + labelSpec.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);
		return Response.ok().build();
	}
	
	@Api(order=600)
	@Path("/{labelSpecId}")
    @DELETE
    public Response deleteSpec(@PathParam("labelSpecId") Long labelSpecId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var labelSpec = labelSpecService.load(labelSpecId);
    	labelSpecService.delete(labelSpec);
		var oldAuditContent = VersionedXmlDoc.fromBean(labelSpec).toXML();
		auditService.audit(null, "deleted label spec \"" + labelSpec.getName() + "\" via RESTful API", oldAuditContent, null);
    	return Response.ok().build();
    }
	
}
