
package	io.onedev.server.web.page.project.blob.render.source;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.clipboard.ClipboardResourceReference;
import io.onedev.server.web.asset.codemirror.CodeMirrorResourceReference;
import io.onedev.server.web.asset.codeproblem.CodeProblemResourceReference;
import io.onedev.server.web.asset.commentindicator.CommentIndicatorCssResourceReference;
import io.onedev.server.web.asset.cookies.CookiesResourceReference;
import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.asset.jqueryui.JQueryUIResourceReference;
import io.onedev.server.web.asset.selectbytyping.SelectByTypingResourceReference;
import io.onedev.server.web.asset.selectionpopover.SelectionPopoverResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

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
		dependencies.add(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeProblemResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(SourceViewResourceReference.class, "source-view.css")));
		dependencies.add(CssHeaderItem.forReference(new CommentIndicatorCssResourceReference()));
		
		return dependencies;
	}

}
