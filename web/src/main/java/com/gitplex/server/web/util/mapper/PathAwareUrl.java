package com.gitplex.server.web.util.mapper;

import java.nio.charset.Charset;

import org.apache.wicket.request.Url;
import org.apache.wicket.util.encoding.UrlEncoder;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

public class PathAwareUrl extends Url {

	private static final long serialVersionUID = 1L;

	public PathAwareUrl(Url url) {
		super(url);
	}
	
	@Override
	public String getPath(Charset charset) {
		StringBuilder path = new StringBuilder();
		boolean slash = false;

		for (String segment : getSegments()) {
			if (slash) {
				path.append('/');
			}
			if (segment.indexOf('/') != -1) {
				Url url = new Url(Splitter.on('/').splitToList(segment), Charsets.UTF_8);
				path.append(url.getPath());
			} else {
				path.append(UrlEncoder.PATH_INSTANCE.encode(segment, charset));
			}
			slash = true;
		}
		return path.toString();
	}

}
