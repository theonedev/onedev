package com.pmease.gitop.web.page.project.source.blob.renderer.highlighter;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class CodeMirrorResourceReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public CodeMirrorResourceReference() {
		super(CodeMirrorResourceReference.class, "res/codemirror/lib/codemirror.js");
	}

	private static ResourceReference CSS = new CssResourceReference(CodeMirrorResourceReference.class, "res/codemirror/lib/codemirror.css");
	
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.of(
						CssHeaderItem.forReference(CSS)
						));
	}
	
	private static final CodeMirrorResourceReference instance =
			new CodeMirrorResourceReference();
	
	public static CodeMirrorResourceReference getInstance() {
		return instance;
	}
}
