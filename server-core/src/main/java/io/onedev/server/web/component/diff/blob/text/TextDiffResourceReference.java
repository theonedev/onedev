package io.onedev.server.web.component.diff.blob.text;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.clipboard.ClipboardResourceReference;
import io.onedev.server.web.asset.codemirror.CodeThemeCssResourceReference;
import io.onedev.server.web.asset.codeproblem.CodeProblemResourceReference;
import io.onedev.server.web.asset.commentindicator.CommentIndicatorCssResourceReference;
import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import io.onedev.server.web.asset.selectionpopover.SelectionPopoverResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

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
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeProblemResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CodeThemeCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(TextDiffPanel.class, "text-diff.css")));
		dependencies.add(CssHeaderItem.forReference(new CommentIndicatorCssResourceReference()));
		return dependencies;
	}

}
