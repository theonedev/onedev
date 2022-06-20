package io.onedev.server.web.component.diff.blob.text;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.clipboard.ClipboardResourceReference;
import io.onedev.server.web.asset.codeproblem.CodeProblemResourceReference;
import io.onedev.server.web.asset.commentindicator.CommentIndicatorCssResourceReference;
import io.onedev.server.web.asset.diff.DiffResourceReference;
import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.asset.selectionpopover.SelectionPopoverResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BlobTextDiffResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BlobTextDiffResourceReference() {
		super(BlobTextDiffResourceReference.class, "blob-text-diff.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new DiffResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new SelectionPopoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeProblemResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(BlobTextDiffPanel.class, "blob-text-diff.css")));
		dependencies.add(CssHeaderItem.forReference(new CommentIndicatorCssResourceReference()));
		return dependencies;
	}

}
