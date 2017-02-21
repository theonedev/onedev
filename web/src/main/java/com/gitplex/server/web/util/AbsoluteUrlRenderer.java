package com.gitplex.server.web.util;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;

import com.google.common.collect.Lists;

public class AbsoluteUrlRenderer extends UrlRenderer {
	
	public AbsoluteUrlRenderer(Request request) {
		super(request);
	}

	@Override
	public String renderRelativeUrl(Url url) {
		if (url.isContextAbsolute()) {
			return url.toString();
		} else {
			Url absolute = new Url(url);
			absolute.prependLeadingSegments(Lists.newArrayList(""));
			return absolute.toString();
		}
	}

	@Override
	public String renderContextRelativeUrl(String url) {
		if (url.startsWith("/")) {
			return url;
		} else {
			Url absolute = Url.parse(url);
			absolute.prependLeadingSegments(Lists.newArrayList(""));
			return absolute.toString();
		}
	}

}