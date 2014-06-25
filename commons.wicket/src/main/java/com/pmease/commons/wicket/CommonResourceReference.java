package com.pmease.commons.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.asset.Asset;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCssReference;

/**
 * Common resource is intended to provide a common look&feel for all products using 
 * commons.wicket 
 * 
 * @author robin
 *
 */
class CommonResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	private List<HeaderItem> bootstrapHeaderItems = new ArrayList<>();
	
	public static CommonResourceReference get() {
		return INSTANCE;
	}
	
	private static CommonResourceReference INSTANCE = new CommonResourceReference();
	
	private CommonResourceReference() {
		super(new Key(Asset.COMMON_JS));
		
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

		dependencies.add(JavaScriptHeaderItem.forReference(Bootstrap.getSettings().getModernizrResourceReference()));
		dependencies.addAll(bootstrapHeaderItems);

		dependencies.add(JavaScriptHeaderItem.forReference(Asset.ARE_YOU_SURE_JS));
		
		dependencies.add(CssHeaderItem.forReference(FontAwesomeCssReference.instance()));
		dependencies.add(CssHeaderItem.forReference(Asset.COMMON_CSS));

		return dependencies;
	}

}
