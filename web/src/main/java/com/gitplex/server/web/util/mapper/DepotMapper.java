package com.gitplex.server.web.util.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

public class DepotMapper extends WebPageMapper {

	public DepotMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
		super(mountPath, pageClass);
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		if (MapperUtils.getDepotSegments(request.getUrl()) > 2)
			return super.mapRequest(request);
		else
			return null;
	}

}
