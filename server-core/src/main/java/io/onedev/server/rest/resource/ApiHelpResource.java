package io.onedev.server.rest.resource;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.onedev.server.rest.ApiHelpJson;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.page.help.ApiHelpUtils;

/** Machine-readable counterpart of the resource / method hierarchy in the help UI. */
@Api(internal = true)
@Path("/help")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ApiHelpResource {

	private final SettingService settingService;

	@Inject
	public ApiHelpResource(SettingService settingService) {
		this.settingService = settingService;
	}

	@GET
	public Map<String, Object> getResources() {
		return new ApiHelpJson().getResources(settingService.getSystemSetting().getServerUrl());
	}

	@GET
	@Path("/{resource}")
	public Map<String, Object> getResource(@PathParam("resource") String resource) {
		return new ApiHelpJson().getResource(findResource(resource));
	}

	@GET
	@Path("/{resource}/{method}")
	public Map<String, Object> getMethod(@PathParam("resource") String resource, @PathParam("method") String method) {
		var resourceClass = findResource(resource);
		for (var resourceMethod: ApiHelpUtils.getResourceMethods(resourceClass)) {
			if (resourceMethod.getName().equals(method))
				return new ApiHelpJson().getMethod(resourceClass, resourceMethod);
		}
		throw new NotFoundException("API method not found: " + method);
	}

	private Class<?> findResource(String resource) {
		// Resolve only documented, registered resources. Never load a caller-supplied class.
		for (var resourceClass: ApiHelpUtils.getResourceClasses()) {
			if (resourceClass.getName().equals(resource))
				return resourceClass;
		}
		throw new NotFoundException("API resource not found: " + resource);
	}

}
