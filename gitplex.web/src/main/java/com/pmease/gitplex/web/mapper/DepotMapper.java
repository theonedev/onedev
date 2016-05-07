package com.pmease.gitplex.web.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

import com.pmease.commons.wicket.NoVersionMountedMapper;

public class DepotMapper extends NoVersionMountedMapper {

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
