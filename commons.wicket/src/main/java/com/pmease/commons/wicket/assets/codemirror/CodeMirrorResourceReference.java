package com.pmease.commons.wicket.assets.codemirror;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmease.commons.wicket.assets.uri.URIResourceReference;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class CodeMirrorResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final CodeMirrorResourceReference INSTANCE = new CodeMirrorResourceReference();
	
	private CodeMirrorResourceReference() {
		super(CodeMirrorResourceReference.class, "codemirror.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(), ImmutableList.<HeaderItem>of(
					JavaScriptHeaderItem.forReference(CodeMirrorCoreResourceReference.INSTANCE),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/dialog/dialog.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/foldcode.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/foldgutter.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/brace-fold.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/xml-fold.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/markdown-fold.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/fold/comment-fold.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/searchcursor.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/search.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/search/matchesonscrollbar.js")),
					JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "matchesonscrollbar2.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/selection/active-line.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/scroll/annotatescrollbar.js")),
					JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/addon/display/fullscreen.js")),
					JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "annotatescrollbar2.js")),
					JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "simplescrollbars.js")),
					JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "gotoline.js")),
					
					JavaScriptHeaderItem.forReference(URIResourceReference.INSTANCE),
					JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "identifier-highlighter.js")),
					
					CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/dialog/dialog.css")),
					CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/fold/foldgutter.css")),
					CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/scroll/simplescrollbars.css")),
					CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/search/matchesonscrollbar.css")),
					CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/addon/display/fullscreen.css")),
					
					CssHeaderItem.forReference(new CssResourceReference(CodeMirrorResourceReference.class, "codemirror.css"))
				));
	}

}
