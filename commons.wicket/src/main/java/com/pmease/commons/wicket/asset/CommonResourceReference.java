package com.pmease.commons.wicket.asset;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;
import com.vaynberg.wicket.select2.ApplicationSettings;

/**
 * Common resource is intended to provide a common look&feel for all products using 
 * commons.wicket 
 * 
 * @author robin
 *
 */
public class CommonResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static CommonResourceReference get() {
		return INSTANCE;
	}
	
	private static CommonResourceReference INSTANCE = new CommonResourceReference();
	
	private CommonResourceReference() {
		super(CommonResourceReference.class, "common.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> headerItems = new ArrayList<>();
		headerItems.add(BootstrapHeaderItem.get());
		
		ApplicationSettings select2Settings = ApplicationSettings.get();
		headerItems.add(JavaScriptHeaderItem.forReference(select2Settings.getMouseWheelReference()));
		headerItems.add(JavaScriptHeaderItem.forReference(select2Settings.getJavaScriptReference()));
		headerItems.add(CssHeaderItem.forReference(select2Settings.getCssReference()));
		headerItems.add(CssHeaderItem.forReference(new CssResourceReference(CommonResourceReference.class, "select2-bootstrap.css")));
		
		headerItems.add(CssHeaderItem.forReference(new CssResourceReference(CommonResourceReference.class, "common.css")));
		return headerItems;
	}

}
