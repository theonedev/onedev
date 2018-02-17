package com.turbodev.server.web.asset.fontext;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

public class FontExtResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public FontExtResourceReference() {
		super(FontExtResourceReference.class, "fontext.css");
	}

	/* 
	 * FontExt overrides part of FontAwesome and we should make it appearing after
	 * FontAwesome 
	 */
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new WebjarsCssResourceReference(
				"font-awesome/current/css/font-awesome.min.css")));
		return dependencies;
	}
	
}
