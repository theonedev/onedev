package com.turbodev.server.web.component.diff.blob.text;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.turbodev.server.web.assets.js.clipboard.ClipboardResourceReference;
import com.turbodev.server.web.assets.js.hover.HoverResourceReference;
import com.turbodev.server.web.assets.js.selectionpopover.SelectionPopoverResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

public class TextDiffResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public TextDiffResourceReference() {
		super(TextDiffResourceReference.class, "text-diff.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new SelectionPopoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/theme/eclipse.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(TextDiffPanel.class, "text-diff.css")));
		return dependencies;
	}

}
