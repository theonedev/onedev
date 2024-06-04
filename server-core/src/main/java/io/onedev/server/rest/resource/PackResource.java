package io.onedev.server.rest.resource;

import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.model.PackLabel;
import io.onedev.server.rest.InvalidParamException;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Api(order=4300, name="Package")
@Path("/packages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PackResource {

	private final PackManager packManager;
	
	@Inject
	public PackResource(PackManager packManager) {
		this.packManager = packManager;
	}

	@Api(order=100)
	@Path("/{packId}")
    @GET
    public Pack getBasicInfo(@PathParam("packId") Long packId) {
		Pack pack = packManager.load(packId);
    	if (!SecurityUtils.canReadPack(pack.getProject())) 
			throw new UnauthorizedException();
    	return pack;
    }

	@Api(order=150, description = "Get list of <a href='/~help/api/io.onedev.server.rest.PackLabelResource'>labels</a>")
	@Path("/{packId}/labels")
	@GET
	public Collection<PackLabel> getLabels(@PathParam("packId") Long packId) {
		Pack pack = packManager.load(packId);
		if (!SecurityUtils.canReadPack(pack.getProject()))
			throw new UnauthorizedException();
		return pack.getLabels();
	}
	
	@Api(order=300)
	@Path("/{packId}/blobs")
    @GET
    public Collection<PackBlob> getBlobs(@PathParam("packId") Long packId) {
		Pack pack = packManager.load(packId);
    	if (!SecurityUtils.canReadPack(pack.getProject())) 
			throw new UnauthorizedException();
    	return pack.getBlobReferences().stream().map(PackBlobReference::getPackBlob).collect(toList());
    }
	
	@Api(order=600)
	@GET
    public List<Pack> queryBasicInfo(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as in <a href='/~packages'>packages page</a>", example="\"Type\" is \"Container Image\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {

		if (!SecurityUtils.isAdministrator() && count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	PackQuery parsedQuery;
		try {
			parsedQuery = PackQuery.parse(null, query, true);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return packManager.query(null, parsedQuery, false, offset, count);
    }
	
	@Api(order=700)
	@Path("/{packId}")
    @DELETE
    public Response delete(@PathParam("packId") Long packId) {
    	Pack pack = packManager.load(packId);
    	if (!SecurityUtils.canWritePack(pack.getProject()))
			throw new UnauthorizedException();
    	packManager.delete(pack);
    	return Response.ok().build();
    }
	
}
