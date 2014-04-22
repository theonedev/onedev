package com.pmease.commons.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.Lists;
import com.vaynberg.wicket.select2.ApplicationSettings;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.references.ModernizrJavaScriptReference;

/**
 * Common resource is intended to provide a common look&feel for all products using 
 * commons.wicket 
 * 
 * @author robin
 *
 */
class CommonResourcesReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	private List<HeaderItem> bootstrapHeaderItems = new ArrayList<>();
	
	public static CommonResourcesReference get() {
		return INSTANCE;
	}
	
	private static CommonResourcesReference INSTANCE = new CommonResourcesReference();
	
	private CommonResourcesReference() {
		super(CommonResourcesReference.class, "asset/common.js");
		
		Bootstrap.renderHead(new IHeaderResponse() {

			@Override
			public void render(HeaderItem item) {
				bootstrapHeaderItems.add(item);
			}

			@Override
			public void markRendered(Object object) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean wasRendered(Object object) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Response getResponse() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isClosed() {
				throw new UnsupportedOperationException();
			}
			
		});
				
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<HeaderItem>();
		
		dependencies.addAll(Lists.newArrayList(super.getDependencies()));

		dependencies.add(JavaScriptHeaderItem.forReference(ModernizrJavaScriptReference.INSTANCE));
		dependencies.addAll(bootstrapHeaderItems);

		ApplicationSettings select2Settings = ApplicationSettings.get();
		dependencies.add(JavaScriptHeaderItem.forReference(select2Settings.getMouseWheelReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(select2Settings.getJavaScriptReference()));
		dependencies.add(CssHeaderItem.forReference(select2Settings.getCssReference()));
		
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CommonResourcesReference.class, "asset/select2-bootstrap.css")));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(CommonResourcesReference.class, "asset/common.css")));

		return dependencies;
	}

}
