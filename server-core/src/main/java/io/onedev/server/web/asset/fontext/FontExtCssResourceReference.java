package io.onedev.server.web.asset.fontext;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.fontawesome.FontAwesomeCssResourceReference;

public class FontExtCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public FontExtCssResourceReference() {
		super(FontExtCssResourceReference.class, "fontext.css");
	}

	/* 
	 * FontExt overrides part of FontAwesome and we should make it appearing after
	 * FontAwesome 
	 */
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new FontAwesomeCssResourceReference()));
		return dependencies;
	}
	
}
