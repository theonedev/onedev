package io.onedev.server.rest;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.entitymanager.IssueQuerySettingManager;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=5100)
@Path("/issue-query-settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueQuerySettingResource {

	private final IssueQuerySettingManager querySettingManager;

	@Inject
	public IssueQuerySettingResource(IssueQuerySettingManager querySettingManager) {
		this.querySettingManager = querySettingManager;
	}

	@Api(order=100)
	@Path("/{querySettingId}")
	@GET
	public IssueQuerySetting get(@PathParam("querySettingId") Long querySettingId) {
		IssueQuerySetting querySetting = querySettingManager.load(querySettingId);
    	if (!SecurityUtils.isAdministrator() && !querySetting.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return querySetting;
	}
	
	@Api(order=200, description="Update issue query setting of specified id in request body, or create new if id property not provided")
	@POST
	public Long createOrUpdate(@NotNull IssueQuerySetting querySetting) {
    	if (!SecurityUtils.canAccess(querySetting.getProject()) 
    			|| !SecurityUtils.isAdministrator() && !querySetting.getUser().equals(SecurityUtils.getUser())) { 
			throw new UnauthorizedException();
    	}
		querySettingManager.save(querySetting);
		return querySetting.getId();
	}
	
	@Api(order=300)
	@Path("/{querySettingId}")
	@DELETE
	public Response delete(@PathParam("querySettingId") Long querySettingId) {
		IssueQuerySetting querySetting = querySettingManager.load(querySettingId);
    	if (!SecurityUtils.isAdministrator() && !querySetting.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		querySettingManager.delete(querySetting);
		return Response.ok().build();
	}
	
}
