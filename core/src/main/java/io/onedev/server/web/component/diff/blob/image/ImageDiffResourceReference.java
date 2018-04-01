package io.onedev.server.web.component.diff.blob.image;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ImageDiffResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ImageDiffResourceReference() {
		super(ImageDiffResourceReference.class, "image-diff.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(ImageDiffPanel.class, "image-diff.css")));
		return dependencies;
	}

}
