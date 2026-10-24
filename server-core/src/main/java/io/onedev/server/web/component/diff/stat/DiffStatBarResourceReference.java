package io.onedev.server.web.component.diff.stat;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class DiffStatBarResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public DiffStatBarResourceReference() {
		super(DiffStatBarResourceReference.class, "diff-stat-bar.css");
	}

}
