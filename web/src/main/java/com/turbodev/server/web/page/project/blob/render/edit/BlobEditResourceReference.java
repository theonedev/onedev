package com.turbodev.server.web.page.project.blob.render.edit;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.turbodev.server.web.assets.js.codemirror.CodeMirrorResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

public class BlobEditResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BlobEditResourceReference() {
		super(BlobEditResourceReference.class, "blob-edit.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(BlobEditResourceReference.class, "blob-edit.css")));
		return dependencies;
	}

}
