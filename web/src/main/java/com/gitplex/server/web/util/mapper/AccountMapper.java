package com.gitplex.server.web.util.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

import com.gitplex.server.web.util.NoVersionMountedMapper;

public class AccountMapper extends NoVersionMountedMapper {

	public AccountMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
		super(mountPath, pageClass);
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		if (MapperUtils.getAccountSegments(request.getUrl()) > 1)
			return super.mapRequest(request);
		else
			return null;
	}

}
