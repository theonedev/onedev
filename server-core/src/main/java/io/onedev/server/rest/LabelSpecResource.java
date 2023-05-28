package io.onedev.server.rest;

import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(order=10200)
@Path("/label-specs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class LabelSpecResource {

	private final LabelSpecManager labelSpecManager;
	
	@Inject
	public LabelSpecResource(LabelSpecManager labelSpecManager) {
		this.labelSpecManager = labelSpecManager;
	}

	@Api(order=100)
	@Path("/{labelSpecId}")
    @GET
    public LabelSpec get(@PathParam("labelSpecId") Long labelSpecId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return labelSpecManager.load(labelSpecId);
    }
	
	@Api(order=400)
	@GET
    public List<LabelSpec> query(@QueryParam("name") String name, 
								 @QueryParam("offset") @Api(example="0") int offset, 
								 @QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

		EntityCriteria<LabelSpec> criteria = EntityCriteria.of(LabelSpec.class);
		if (name != null) 
			criteria.add(Restrictions.ilike("name", name.replace('*', '%'), MatchMode.EXACT));
		
    	return labelSpecManager.query(criteria, offset, count);
    }
	
	@Api(order=500, description="Create new label spec")
    @POST
    public Long create(@NotNull LabelSpec labelSpec) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		labelSpecManager.create(labelSpec);
    	return labelSpec.getId();
    }

	@Api(order=550, description="Update label spec of specified id")
	@Path("/{labelSpecId}")
	@POST
	public Response update(@PathParam("labelSpecId") Long labelSpecId, @NotNull LabelSpec labelSpec) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		labelSpecManager.update(labelSpec);
		return Response.ok().build();
	}
	
	@Api(order=600)
	@Path("/{labelSpecId}")
    @DELETE
    public Response delete(@PathParam("labelSpecId") Long labelSpecId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	labelSpecManager.delete(labelSpecManager.load(labelSpecId));
    	return Response.ok().build();
    }
	
}
