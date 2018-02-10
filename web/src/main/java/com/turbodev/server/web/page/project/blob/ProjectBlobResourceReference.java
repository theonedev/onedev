package com.turbodev.server.web.page.project.blob;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.turbodev.server.web.assets.js.cookies.CookiesResourceReference;
import com.turbodev.server.web.assets.js.hotkeys.HotkeysResourceReference;
import com.turbodev.server.web.assets.js.jqueryui.JQueryUIResourceReference;
import com.turbodev.server.web.page.base.BaseDependentCssResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

public class ProjectBlobResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ProjectBlobResourceReference() {
		super(ProjectBlobResourceReference.class, "project-blob.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(ProjectBlobResourceReference.class, "project-blob.css")));
		return dependencies;
	}

}
