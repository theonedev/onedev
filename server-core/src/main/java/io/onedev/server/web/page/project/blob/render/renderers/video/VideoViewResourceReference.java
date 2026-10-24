package io.onedev.server.web.page.project.blob.render.renderers.video;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class VideoViewResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public VideoViewResourceReference() {
		super(VideoViewResourceReference.class, "video-view.css");
	}

}
