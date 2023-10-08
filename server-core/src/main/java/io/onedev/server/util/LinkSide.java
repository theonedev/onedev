package io.onedev.server.util;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.manager.LinkSpecManager;
import io.onedev.server.model.LinkSpec;

public class LinkSide {
	
	private final LinkSpec spec;
	
	private final boolean opposite;
	
	public LinkSide(LinkSpec spec, boolean opposite) {
		this.spec = spec;
		this.opposite = opposite;
	}
	
	public LinkSide(String linkName) {
		spec = OneDev.getInstance(LinkSpecManager.class).find(linkName);
		if (spec == null)
			throw new ExplicitException("Link spec not found: " + linkName);
		opposite = !linkName.equals(spec.getName());
	}
	
	public LinkSpec getSpec() {
		return spec;
	}

	public boolean isOpposite() {
		return opposite;
	}
	
	public String getName() {
		return spec.getName(opposite);
	}
	
}