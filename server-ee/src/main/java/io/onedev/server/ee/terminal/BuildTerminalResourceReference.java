package io.onedev.server.ee.terminal;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.xterm.XtermResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BuildTerminalResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BuildTerminalResourceReference() {
		super(BuildTerminalResourceReference.class, "build-terminal.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new XtermResourceReference()));
		return dependencies;
	}
	
}
