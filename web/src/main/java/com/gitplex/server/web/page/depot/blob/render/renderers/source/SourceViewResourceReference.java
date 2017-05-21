package com.gitplex.server.web.page.depot.blob.render.renderers.source;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.server.web.assets.js.clipboard.ClipboardResourceReference;
import com.gitplex.server.web.assets.js.codemirror.CodeMirrorResourceReference;
import com.gitplex.server.web.assets.js.cookies.CookiesResourceReference;
import com.gitplex.server.web.assets.js.doneevents.DoneEventsResourceReference;
import com.gitplex.server.web.assets.js.hotkeys.HotkeysResourceReference;
import com.gitplex.server.web.assets.js.hover.HoverResourceReference;
import com.gitplex.server.web.assets.js.jqueryui.JQueryUIResourceReference;
import com.gitplex.server.web.assets.js.scrollintoview.ScrollIntoViewResourceReference;
import com.gitplex.server.web.assets.js.selectionpopover.SelectionPopoverResourceReference;
import com.gitplex.server.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;

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
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(SourceViewResourceReference.class, "source-view.css")));
		return dependencies;
	}

}
