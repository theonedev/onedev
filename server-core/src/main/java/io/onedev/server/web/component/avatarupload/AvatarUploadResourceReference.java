package io.onedev.server.web.component.avatarupload;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.cropper.CropperResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class AvatarUploadResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public AvatarUploadResourceReference() {
		super(AvatarUploadResourceReference.class, "avatar-upload.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CropperResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				AvatarUploadResourceReference.class, "avatar-upload.css")));
		return dependencies;
	}

}
