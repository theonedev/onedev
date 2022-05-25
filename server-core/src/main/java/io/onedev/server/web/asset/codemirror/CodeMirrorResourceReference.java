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
		super(CodeMirrorResourceLocator.class, "codemirror-integration.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "lib/codemirror.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/mode/overlay.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/mode/simple.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/mode/multiplex.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/edit/matchbrackets.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/dialog/dialog.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/fold/foldcode.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/fold/foldgutter.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/fold/brace-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/fold/xml-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/fold/markdown-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/fold/comment-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/search/searchcursor.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/search/search.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/search/matchesonscrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				CodeMirrorResourceLocator.class, "matchesonscrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/selection/active-line.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/scroll/annotatescrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/display/fullscreen.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/selection/mark-selection.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "addon/hint/show-hint.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				CodeMirrorResourceLocator.class, "annotatescrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				CodeMirrorResourceLocator.class, "simplescrollbars.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				CodeMirrorResourceLocator.class, "gotoline.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				CodeMirrorResourceLocator.class, "identifier-highlighter.js"))); 
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				CodeMirrorResourceLocator.class, "addon/mode/loadmode.js"))); 
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				CodeMirrorResourceLocator.class, "mode/meta.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new ModeUrlResourceReference()));
		
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceLocator.class, "lib/codemirror.css")));
		dependencies.add(CssHeaderItem.forReference(new CodeThemeCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceLocator.class, "addon/dialog/dialog.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceLocator.class, "addon/fold/foldgutter.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceLocator.class, "addon/scroll/simplescrollbars.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceLocator.class, "addon/search/matchesonscrollbar.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceLocator.class, "addon/display/fullscreen.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeMirrorResourceLocator.class, "addon/hint/show-hint.css")));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				CodeMirrorResourceLocator.class, "codemirror-custom.css")));

		return dependencies;
	}
}
