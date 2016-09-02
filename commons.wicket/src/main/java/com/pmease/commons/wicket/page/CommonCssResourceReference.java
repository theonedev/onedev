package com.pmease.commons.wicket.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import de.agilecoders.wicket.core.Bootstrap;

/**
 * Common resource is intended to provide a common look&feel for all products using 
 * commons.wicket 
 * 
 * @author robin
 *
 */
public class CommonCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public CommonCssResourceReference() {
		super(CommonCssResourceReference.class, "common.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<HeaderItem>();
		dependencies.add(CssHeaderItem.forReference(Bootstrap.getSettings().getCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new FontAwesomeResourceReference()));
		return dependencies;
	}

}
