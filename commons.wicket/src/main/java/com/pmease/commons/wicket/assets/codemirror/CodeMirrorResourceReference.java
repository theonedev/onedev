package com.pmease.commons.wicket.assets.codemirror;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class CodeMirrorResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final CodeMirrorResourceReference INSTANCE = new CodeMirrorResourceReference();
	
	private CodeMirrorResourceReference() {
		super(CodeMirrorResourceReference.class, "codemirror.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(CodeMirrorCoreResourceReference.INSTANCE));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/dialog/dialog.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/foldcode.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/foldgutter.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/brace-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/xml-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/markdown-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/comment-fold.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/searchcursor.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/search.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/matchesonscrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CodeMirrorResourceReference.class, "matchesonscrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/selection/active-line.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/scroll/annotatescrollbar.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("codemirror/current/addon/display/fullscreen.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CodeMirrorResourceReference.class, "annotatescrollbar2.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CodeMirrorResourceReference.class, "simplescrollbars.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CodeMirrorResourceReference.class, "gotoline.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CodeMirrorResourceReference.class, "identifier-highlighter.js")));
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/addon/dialog/dialog.css")));
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/addon/fold/foldgutter.css")));
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/addon/scroll/simplescrollbars.css")));
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/addon/search/matchesonscrollbar.css")));
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("codemirror/current/addon/display/fullscreen.css")));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(CodeMirrorResourceReference.class, "codemirror.css")));
		return dependencies;
	}

}
