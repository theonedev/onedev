package com.pmease.commons.wicket.behavior.clipboard;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.pmease.commons.wicket.assets.clipboard.ClipboardResourceReference;
import com.pmease.commons.wicket.page.CommonDependentResourceReference;

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
