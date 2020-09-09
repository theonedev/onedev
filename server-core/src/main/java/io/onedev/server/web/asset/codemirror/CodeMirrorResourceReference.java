package io.onedev.server.web.asset.codemirror;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.asset.hotkeys.HotkeysResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;
import io.onedev.server.web.resourcebundle.ResourceBundle;

@ResourceBundle
public class CodeMirrorResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeMirrorResourceReference() {
		super(CodeMirrorResourceReference.class, "codemirror-integration.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "lib/codemirror.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/mode/overlay.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/mode/simple.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/mode/multiplex.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/edit/matchbrackets.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/dialog/dialog.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/fold/foldcode.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/fold/foldgutter.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/fold/brace-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/fold/xml-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/fold/markdown-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/fold/comment-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/search/searchcursor.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/search/search.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/search/matchesonscrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "matchesonscrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/selection/active-line.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/scroll/annotatescrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/display/fullscreen.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/selection/mark-selection.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "addon/hint/show-hint.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "annotatescrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "simplescrollbars.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "gotoline.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "identifier-highlighter.js"))); 
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "loadmode.js"))); 
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "mode/meta.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new ModeUrlResourceReference()));
		
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "lib/codemirror.css")));
		dependencies.add(CssHeaderItem.forReference(new CodeThemeCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "addon/dialog/dialog.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "addon/fold/foldgutter.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "addon/scroll/simplescrollbars.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "addon/search/matchesonscrollbar.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "addon/display/fullscreen.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "addon/hint/show-hint.css")));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(CodeMirrorResourceReference.class, "codemirror-custom.css")));

		return dependencies;
	}
}
