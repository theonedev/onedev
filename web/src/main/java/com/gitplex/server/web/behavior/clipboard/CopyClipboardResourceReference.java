package com.gitplex.server.web.behavior.clipboard;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.server.web.assets.clipboard.ClipboardResourceReference;
import com.gitplex.server.web.page.CommonDependentResourceReference;

public class CopyClipboardResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CopyClipboardResourceReference() {
		super(CopyClipboardResourceReference.class, "copy-clipboard.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		return dependencies;
	}

}
