package io.onedev.server.model.support;

import io.onedev.agent.job.ImageMappingFacade;
import io.onedev.server.annotation.Editable;

import java.io.Serializable;

@Editable
public class ImageMapping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String from;
	
	private String to;

	@Editable(order=100, description = "A Java regular expression matching image (registry/repo:tag)")
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@Editable(order=200, description = "Target image to use. Note that group reference can be used to " +
			"refer to part of original image based on defined from pattern")
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	public ImageMappingFacade getFacade() {
		return new ImageMappingFacade(from, to);
	}
	
}
