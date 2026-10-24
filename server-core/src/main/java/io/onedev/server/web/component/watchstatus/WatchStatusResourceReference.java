package io.onedev.server.web.component.watchstatus;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class WatchStatusResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public WatchStatusResourceReference() {
		super(WatchStatusResourceReference.class, "watch-status.css");
	}
	
}
