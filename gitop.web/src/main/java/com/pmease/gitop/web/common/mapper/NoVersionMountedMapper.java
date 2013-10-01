package com.pmease.gitop.web.common.mapper;

import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.util.IProvider;

/**
 * Provides a mount strategy that drops the version number from stateful page
 * urls.
 */
public class NoVersionMountedMapper extends MountedMapper {
	
	public NoVersionMountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass,
			IPageParametersEncoder pageParametersEncoder) {
		super(mountPath, pageClass, pageParametersEncoder);
	}

	public NoVersionMountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
		super(mountPath, pageClass);
	}

	public NoVersionMountedMapper(String mountPath, IProvider<Class<? extends IRequestablePage>> pageClassProvider,
			IPageParametersEncoder pageParametersEncoder) {
		super(mountPath, pageClassProvider, pageParametersEncoder);
	}

	public NoVersionMountedMapper(String mountPath, IProvider<Class<? extends IRequestablePage>> pageClassProvider) {
		super(mountPath, pageClassProvider);
	}

	@Override
	protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
		// do nothing so that component info does not get rendered in url
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		if (requestHandler instanceof ListenerInterfaceRequestHandler) {
			return null;
		} else {
			return super.mapHandler(requestHandler);
		}
	}
}