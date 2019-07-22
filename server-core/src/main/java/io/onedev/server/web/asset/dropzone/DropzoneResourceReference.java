package io.onedev.server.web.asset.dropzone;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class DropzoneResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public DropzoneResourceReference() {
		super(DropzoneResourceReference.class, "dropzone.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(
				Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(DropzoneResourceReference.class, "dropzone.css")));
		dependencies.add(OnDomReadyHeaderItem.forScript("Dropzone.autoDiscover = false;"));
		
		return dependencies;
	}
	
}
