package com.pmease.gitop.web.common.wicket.mapper;

import org.apache.wicket.core.util.resource.PackageResourceStream;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.apache.wicket.util.lang.Args;

public class StaticResourceMapper extends AbstractMapper {

	private final String[] mountSegments;

	private final Class<?> scope;
	
	public StaticResourceMapper(String path, Class<?> scope) {
		Args.notEmpty(path, "path");
		Args.notNull(scope, "scope");
		
		this.scope = scope;
		this.mountSegments = getMountSegments(path);
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		final Url url = new Url(request.getUrl());
		
		if (!urlStartsWith(url, mountSegments)) {
			return null;
		}
		
		url.removeLeadingSegments(mountSegments.length);
		
		return new ResourceStreamRequestHandler(new PackageResourceStream(scope, url.getPath()));
	}

	@Override
	public int getCompatibilityScore(Request request) {
		return 0;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		return null;
	}
}