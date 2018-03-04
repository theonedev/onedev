package com.turbodev.server.web.component.markdown;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.turbodev.server.web.asset.atwho.AtWhoResourceReference;
import com.turbodev.server.web.asset.caret.CaretResourceReference;
import com.turbodev.server.web.asset.codemirror.CodeMirrorResourceReference;
import com.turbodev.server.web.asset.cookies.CookiesResourceReference;
import com.turbodev.server.web.asset.doneevents.DoneEventsResourceReference;
import com.turbodev.server.web.asset.hotkeys.HotkeysResourceReference;
import com.turbodev.server.web.asset.jqueryui.JQueryUIResourceReference;
import com.turbodev.server.web.asset.textareacaretposition.TextareaCaretPositionResourceReference;
import com.turbodev.server.web.page.base.BaseDependentCssResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

@SuppressWarnings("serial")
public class MarkdownResourceReference extends BaseDependentResourceReference {

	public MarkdownResourceReference() {
		super(MarkdownResourceReference.class, "markdown.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CaretResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AtWhoResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new TextareaCaretPositionResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));

		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(MarkdownResourceReference.class, "markdown.css")));
		return dependencies;
	}
	
}
