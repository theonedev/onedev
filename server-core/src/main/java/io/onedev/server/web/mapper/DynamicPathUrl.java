package io.onedev.server.web.mapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.request.Url;
import org.apache.wicket.util.encoding.UrlEncoder;

import com.google.common.base.Splitter;

public class DynamicPathUrl extends Url {

	private static final long serialVersionUID = 1L;

	public DynamicPathUrl(Url url) {
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
				Url url = new Url(Splitter.on('/').splitToList(segment), StandardCharsets.UTF_8);
				path.append(url.getPath());
			} else {
				path.append(UrlEncoder.PATH_INSTANCE.encode(segment, charset));
			}
			slash = true;
		}
		return path.toString();
	}

}
