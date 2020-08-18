package io.onedev.server.web.mapper;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

public class DynamicPathResourceMapper extends BaseResourceMapper {

	public DynamicPathResourceMapper(String path, ResourceReference resourceReference) {
		super(path, resourceReference);
	}

	@Override
	protected Url encodePageParameters(Url url, PageParameters pageParameters, IPageParametersEncoder encoder) {
		return new DynamicPathUrl(super.encodePageParameters(url, pageParameters, encoder));
	}

}
