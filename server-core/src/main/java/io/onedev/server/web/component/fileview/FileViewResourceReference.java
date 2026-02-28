package io.onedev.server.web.component.fileview;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.codemirror.CodeMirrorResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class FileViewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public FileViewResourceReference() {
		super(FileViewResourceReference.class, "file-view.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		return dependencies;
	}

}
