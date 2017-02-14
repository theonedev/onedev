package com.gitplex.server.web.mapper;

import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.resource.ResourceReference;

public class DepotResourceMapper extends ResourceMapper {

	public DepotResourceMapper(String path, ResourceReference resourceReference) {
		super(path, resourceReference);
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		if (MapperUtils.getDepotSegments(request.getUrl()) > 2)
			return super.mapRequest(request);
		else
			return null;
	}

}
