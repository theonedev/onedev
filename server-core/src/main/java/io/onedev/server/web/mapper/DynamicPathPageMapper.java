package io.onedev.server.web.mapper;

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class DynamicPathPageMapper extends MountedMapper {

	public DynamicPathPageMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
		super(mountPath, pageClass);
	}

	@Override
	protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
		if (info.getComponentInfo() != null)
			super.encodePageComponentInfo(url, info);
	}

	@Override
	protected Url encodePageParameters(Url url, PageParameters pageParameters, IPageParametersEncoder encoder) {
		return new DynamicPathUrl(super.encodePageParameters(url, pageParameters, encoder));
	}

}
