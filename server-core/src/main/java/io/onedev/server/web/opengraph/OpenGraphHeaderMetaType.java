package io.onedev.server.web.opengraph;

public enum OpenGraphHeaderMetaType {
	Image,
	Title,
	Description,
	Url;
	
	@Override
	public String toString() {
		switch(this) {
			case Image: return "og:image";
			case Title: return "og:title";
			case Description: return "og:description";
			case Url: return "og:url";
			// Unreachable
			default: return null;
		}
	}
}
