package io.onedev.server.web.util;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.Url.StringMode;
import org.apache.wicket.util.string.Strings;

import com.google.common.collect.Lists;

import io.onedev.server.web.mapper.DynamicPathUrl;

public class AbsoluteUrlRenderer extends UrlRenderer {

	public AbsoluteUrlRenderer(Request request) {
		super(request);
	}

	@Override
	public String renderRelativeUrl(Url url) {
		if (url.isFull()) {
			return url.toString(StringMode.FULL);
		} else if (url.getSegments().isEmpty()) {
			Url absolute = copy(url);
			absolute.prependLeadingSegments(Lists.newArrayList("", ""));
			return absolute.toString();
		} else if (Strings.isEmpty(url.getSegments().get(0))) {
			return url.toString();
		} else {
			Url absolute = copy(url);
			absolute.prependLeadingSegments(Lists.newArrayList(""));
			return absolute.toString();
		}
	}

	private Url copy(Url url) {
		if (url instanceof DynamicPathUrl)
			return new DynamicPathUrl(url);
		else
			return new Url(url);
	}
}