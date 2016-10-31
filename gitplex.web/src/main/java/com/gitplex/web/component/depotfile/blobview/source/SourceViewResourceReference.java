package com.gitplex.web.component.depotfile.blobview.source;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.web.page.base.BaseDependentResourceReference;
import com.gitplex.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.gitplex.commons.wicket.assets.cookies.CookiesResourceReference;
import com.gitplex.commons.wicket.assets.hover.HoverResourceReference;
import com.gitplex.commons.wicket.assets.jqueryui.JQueryUIResourceReference;
import com.gitplex.commons.wicket.assets.selectionpopover.SelectionPopoverResourceReference;

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
		
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(SourceViewResourceReference.class, "source-view.css")));
		return dependencies;
	}

}
