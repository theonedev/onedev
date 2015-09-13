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
import com.pmease.commons.wicket.assets.Assets;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

/**
 * Common resource is intended to provide a common look&feel for all products using 
 * commons.wicket 
 * 
 * @author robin
 *
 */
public class CommonResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	private List<HeaderItem> bootstrapHeaderItems = new ArrayList<>();
	
	public static final CommonResourceReference INSTANCE = new CommonResourceReference();
	
	private CommonResourceReference() {
		super(new Key(Assets.COMMON_JS));
		
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
		
		dependencies.add(CssHeaderItem.forReference(
				new WebjarsCssResourceReference("font-awesome/current/css/font-awesome.min.css")));

		dependencies.add(JavaScriptHeaderItem.forReference(Assets.ALIGN_JS));

		dependencies.add(JavaScriptHeaderItem.forReference(Assets.STICKY_JS));

		dependencies.add(JavaScriptHeaderItem.forReference(Assets.ARE_YOU_SURE_JS));
		
		dependencies.add(CssHeaderItem.forReference(Assets.COMMON_CSS));

		return dependencies;
	}

}
