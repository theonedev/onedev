package io.onedev.server.web.component.commandpalette;

public class FixedSegment implements UrlSegment {

	private static final long serialVersionUID = 1L;
	
	private final String path;
	
	public FixedSegment(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return path;
	}
	
}
