package io.onedev.server.web.component.markdown;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.atwho.AtWhoResourceReference;
import io.onedev.server.web.asset.caret.CaretResourceReference;
import io.onedev.server.web.asset.codemirror.CodeMirrorResourceReference;
import io.onedev.server.web.asset.codemirror.CodeThemeCssResourceReference;
import io.onedev.server.web.asset.cookies.CookiesResourceReference;
import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.hotkeys.HotkeysResourceReference;
import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.asset.jqueryui.JQueryUIResourceReference;
import io.onedev.server.web.asset.textareacaretposition.TextareaCaretPositionResourceReference;
import io.onedev.server.web.component.commit.status.CommitStatusCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

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
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CommitStatusCssResourceReference()));

		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(MarkdownResourceReference.class, "markdown.css")));
		dependencies.add(CssHeaderItem.forReference(new CodeThemeCssResourceReference()));
		return dependencies;
	}
	
}
