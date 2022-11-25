package io.onedev.server.web.mapper;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ProjectPageMapper extends BasePageMapper {

	private final Class<? extends IRequestablePage> pageClass;

	public ProjectPageMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
		super(mountPath, pageClass);
		this.pageClass = pageClass;
	}

	@Override
	protected boolean setPlaceholders(PageParameters parameters, Url url) {
		return ProjectMapperUtils.setPlaceholders(parameters, url, mountSegments, new PlaceholderProvider() {

			@Override
			public String getPlaceholder(String mountSegment) {
				return ProjectPageMapper.this.getPlaceholder(mountSegment);
			}

			@Override
			public String getOptionalPlaceholder(String mountSegment) {
				return ProjectPageMapper.this.getOptionalPlaceholder(mountSegment);
			}
			
		});
	}

	@Override
	protected PageParameters extractPageParameters(final Request request, int segmentsToSkip,
			final IPageParametersEncoder encoder) {
		return ProjectMapperUtils.extractPageParameters(request, segmentsToSkip, encoder);
	}

	@Override
	protected boolean urlStartsWithMountedSegments(Url url) {
		Url normalizedUrl = ProjectMapperUtils.normalize(url);
		if (normalizedUrl == null)
			normalizedUrl = url;
		return super.urlStartsWithMountedSegments(normalizedUrl);
	}

	@Override
	protected boolean urlStartsWith(Url url, String... segments) {
		Url normalizedUrl = ProjectMapperUtils.normalize(url);
		if (normalizedUrl == null)
			normalizedUrl = url;
		return super.urlStartsWith(normalizedUrl, segments);
	}

	@Override
	protected UrlInfo parseRequest(Request request) {
		Url normalizedUrl = ProjectMapperUtils.normalize(request.getUrl());

		if (normalizedUrl != null && urlStartsWithMountedSegments(normalizedUrl)) {
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(normalizedUrl);
			PageParameters pageParameters = extractPageParameters(request, normalizedUrl);

			return new UrlInfo(info, pageClass, pageParameters);
		} else {
			return null;
		}
	}

}
