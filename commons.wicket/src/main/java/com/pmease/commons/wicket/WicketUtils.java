package com.pmease.commons.wicket;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

public class WicketUtils {
	
	public static String relativizeUrl(String url) {
		if (Url.parse(url).isFull())
			return url;
		else
			return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(url);
	}
	
}
