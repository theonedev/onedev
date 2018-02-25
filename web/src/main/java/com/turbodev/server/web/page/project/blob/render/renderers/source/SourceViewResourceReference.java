package com.turbodev.server.web.page.project.blob.render.renderers.source;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.turbodev.server.web.asset.clipboard.ClipboardResourceReference;
import com.turbodev.server.web.asset.codemirror.CodeMirrorResourceReference;
import com.turbodev.server.web.asset.cookies.CookiesResourceReference;
import com.turbodev.server.web.asset.doneevents.DoneEventsResourceReference;
import com.turbodev.server.web.asset.hover.HoverResourceReference;
import com.turbodev.server.web.asset.jqueryui.JQueryUIResourceReference;
import com.turbodev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import com.turbodev.server.web.asset.selectionpopover.SelectionPopoverResourceReference;
import com.turbodev.server.web.page.base.BaseDependentCssResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

public class SourceViewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SourceViewResourceReference() {
		super(SourceViewResourceReference.class, "source-view.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new SelectionPopoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(SourceViewResourceReference.class, "source-view.css")));
		return dependencies;
	}

}
