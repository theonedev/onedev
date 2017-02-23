package com.gitplex.server.web.assets.codemirror;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.server.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;
import com.gitplex.server.web.util.resourcebundle.ResourceBundle;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

@ResourceBundle
public class CodeMirrorResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeMirrorResourceReference() {
		super(CodeMirrorResourceReference.class, "codemirror.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/lib/codemirror.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/mode/overlay.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/mode/loadmode.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/edit/matchbrackets.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/dialog/dialog.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/foldcode.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/foldgutter.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/brace-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/xml-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/markdown-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/comment-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/searchcursor.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/search.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/matchesonscrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "matchesonscrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/selection/active-line.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/scroll/annotatescrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/display/fullscreen.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "annotatescrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "simplescrollbars.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "gotoline.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "identifier-highlighter.js"))); 
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(CodeMirrorResourceReference.class, "highlight.js"))); 
		dependencies.add(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/mode/meta.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new ModeUrlResourceReference()));
		
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/lib/codemirror.css")));
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/theme/eclipse.css")));
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/dialog/dialog.css")));
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/fold/foldgutter.css")));
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/scroll/simplescrollbars.css")));
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/search/matchesonscrollbar.css")));
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/display/fullscreen.css")));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(CodeMirrorResourceReference.class, "highlight.css")));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(CodeMirrorResourceReference.class, "codemirror.css")));

		return dependencies;
	}
}
