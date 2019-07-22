package io.onedev.server.web.component.colorpicker;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.spectrum.SpectrumResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ColorPickerResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ColorPickerResourceReference() {
		super(ColorPickerResourceReference.class, "color-picker.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new SpectrumResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(ColorPickerResourceReference.class, "color-picker.css")));
		return dependencies;
	}

}
