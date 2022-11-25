package io.onedev.server.web.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.ResourceUtil;

import com.google.common.base.Splitter;

public class ProjectResourceMapper extends BaseResourceMapper {

	private final String[] mountSegments;

	private final IPageParametersEncoder parametersEncoder = new PageParametersEncoder();

	public ProjectResourceMapper(String path, ResourceReference resourceReference) {
		super(path, resourceReference);
		mountSegments = getMountSegments(path);
	}

	@Override
	protected boolean setPlaceholders(PageParameters parameters, Url url) {
		return ProjectMapperUtils.setPlaceholders(parameters, url, mountSegments, new PlaceholderProvider() {

			@Override
			public String getPlaceholder(String mountSegment) {
				return ProjectResourceMapper.this.getPlaceholder(mountSegment);
			}

			@Override
			public String getOptionalPlaceholder(String mountSegment) {
				return ProjectResourceMapper.this.getOptionalPlaceholder(mountSegment);
			}

		});
	}

	@Override
	public IRequestHandler mapRequest(final Request request) {
		Url normalizedUrl = ProjectMapperUtils.normalize(request.getUrl());
		if (normalizedUrl == null)
			normalizedUrl = new Url(request.getUrl());			

		// now extract the page parameters from the request url
		PageParameters parameters = extractPageParameters(request, mountSegments.length, parametersEncoder);

		// remove caching information from current request
		removeCachingDecoration(normalizedUrl, parameters);

		// check if url matches mount path
		if (urlStartsWith(normalizedUrl, mountSegments) == false) {
			return null;
		}

		// check if there are placeholders in mount segments
		for (int index = 0; index < mountSegments.length; ++index) {
			String placeholder = getPlaceholder(mountSegments[index]);

			if (placeholder != null) {
				// extract the parameter from URL
				if (parameters == null) {
					parameters = new PageParameters();
				}
				parameters.add(placeholder, normalizedUrl.getSegments().get(index), INamedParameters.Type.PATH);
			}
		}
		return new ResourceReferenceRequestHandler(getResourceReference(), parameters);
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		if ((requestHandler instanceof ResourceReferenceRequestHandler) == false) {
			return null;
		}

		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler) requestHandler;

		// see if request handler addresses the resource reference we serve
		if (getResourceReference().equals(handler.getResourceReference()) == false) {
			return null;
		}

		Url url = new Url();

		// add mount path segments
		for (String segment : mountSegments) {
			url.getSegments().add(segment);
		}

		// replace placeholder parameters
		PageParameters parameters = new PageParameters(handler.getPageParameters());

		url.getSegments().remove(0);
		int start = 0;
		for (String projectName : Splitter.on("/").split(parameters.get(ProjectMapperUtils.PARAM_PROJECT).toString())) {
			url.getSegments().add(start++, projectName);
		}
		parameters.remove(ProjectMapperUtils.PARAM_PROJECT);

		for (int index = 1; index < mountSegments.length; ++index) {
			String placeholder = getPlaceholder(mountSegments[index]);

			if (placeholder != null) {
				url.getSegments().set(index, parameters.get(placeholder).toString(""));
				parameters.remove(placeholder);
			}
		}

		// add caching information
		addCachingDecoration(url, parameters);

		ResourceUtil.encodeResourceReferenceAttributes(url, getResourceReference());
		// create url
		return encodePageParameters(url, parameters, parametersEncoder);
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

}
