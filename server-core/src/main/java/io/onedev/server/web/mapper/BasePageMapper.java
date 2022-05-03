package io.onedev.server.web.mapper;

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;

public class BasePageMapper extends MountedMapper {

	public BasePageMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
		super(mountPath, pageClass);
	}

	@Override
	protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
		if (info.getComponentInfo() != null)
			super.encodePageComponentInfo(url, info);
	}

}
