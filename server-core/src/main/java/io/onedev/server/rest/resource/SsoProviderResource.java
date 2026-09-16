package io.onedev.server.rest.resource;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SsoProviderService;

@Path("/sso-providers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SsoProviderResource {

	@Inject
	private SsoProviderService ssoProviderService;

	@Inject
	private AuditService auditService;
	
	@Api(order=100)
    @GET
	@Path("/{ssoProviderId}")
    public SsoProvider getSsoProvider(@PathParam("ssoProviderId") Long ssoProviderId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return ssoProviderService.load(ssoProviderId);
    }	

	@Api(order=200)
    @GET
    public List<SsoProvider> listSsoProviders() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return ssoProviderService.query();
    }	

	@Api(order=250, description="Get SSO provider id by name")
	@Path("/ids/{name}")
	@GET
	public Long getSsoProviderId(@PathParam("name") String name) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		var ssoProvider = ssoProviderService.find(name);
		if (ssoProvider != null)
			return ssoProvider.getId();
		else
			throw new NotFoundException();
	}

	@Api(order=300, description="Create SSO provider")
    @POST
    public Long createSsoProvider(@NotNull @Valid SsoProvider ssoProvider) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		if (ssoProviderService.find(ssoProvider.getName()) != null)
			throw new NotAcceptableException("Name is already used by another SSO provider");

		ssoProviderService.createOrUpdate(ssoProvider);
		var auditContent = VersionedXmlDoc.fromBean(ssoProvider).toXML();
		auditService.audit(null, "created SSO provider \"" + ssoProvider.getName() + "\" via RESTful API", null, auditContent);

    	return ssoProvider.getId();
    }

	@Api(order=350, description="Update SSO provider of specified id")
	@Path("/{ssoProviderId}")
	@POST
	public Response updateSsoProvider(@PathParam("ssoProviderId") Long ssoProviderId, @NotNull @Valid SsoProvider ssoProvider) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();

		SsoProvider existingSsoProvider = ssoProviderService.find(ssoProvider.getName());
		if (existingSsoProvider != null && !existingSsoProvider.equals(ssoProvider))
			throw new NotAcceptableException("Name is already used by another SSO provider");

		var oldAuditContent = ssoProvider.getOldVersion().toXML();
		var newAuditContent = VersionedXmlDoc.fromBean(ssoProvider).toXML();

		ssoProviderService.createOrUpdate(ssoProvider);

		auditService.audit(null, "changed SSO provider \"" + ssoProvider.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);

		return Response.ok().build();
	}
	
	@Api(order=400)
	@Path("/{ssoProviderId}")
    @DELETE
    public Response deleteSsoProvider(@PathParam("ssoProviderId") Long ssoProviderId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		var ssoProvider = ssoProviderService.load(ssoProviderId);
		var oldAuditContent = VersionedXmlDoc.fromBean(ssoProvider).toXML();
    	ssoProviderService.delete(ssoProvider);
		auditService.audit(null, "deleted SSO provider \"" + ssoProvider.getName() + "\" via RESTful API", oldAuditContent, null);
    	return Response.ok().build();
    }	

}
