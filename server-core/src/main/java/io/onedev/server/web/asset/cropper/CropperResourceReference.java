package io.onedev.server.web.asset.cropper;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class CropperResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public CropperResourceReference() {
		super(CropperResourceReference.class, "cropper.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CropperResourceReference.class, "cropper.min.css")));
		return dependencies;
	}

}
