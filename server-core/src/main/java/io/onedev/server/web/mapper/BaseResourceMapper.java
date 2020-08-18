package io.onedev.server.web.mapper;

import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.request.resource.ResourceReference;

public class BaseResourceMapper extends ResourceMapper {

	private final String path;
	
	private final ResourceReference resourceReference;
	
	public BaseResourceMapper(String path, ResourceReference resourceReference) {
		super(path, resourceReference);
		this.path = path;
		this.resourceReference = resourceReference;
	}

	public String getPath() {
		return path;
	}

	public ResourceReference getResourceReference() {
		return resourceReference;
	}
	
}
