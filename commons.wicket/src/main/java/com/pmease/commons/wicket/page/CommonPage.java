package com.pmease.commons.wicket.page;

import org.apache.wicket.bootstrap.Bootstrap;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.pmease.commons.wicket.asset.Asset;

@SuppressWarnings("serial")
public class CommonPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		/*
		 * Contribute common javascript and css via a separate component to make sure these header items appears 
		 * before any other header items.
		 */
		add(new WebMarkupContainer("resources") {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				Bootstrap.renderHead(response);
				
				PackageResourceReference reference = new PackageResourceReference(Asset.class, "javascript/jquery.align.js");
				response.render(JavaScriptHeaderItem.forReference(reference));
				reference = new PackageResourceReference(Asset.class, "javascript/common.js");
				response.render(JavaScriptHeaderItem.forReference(reference));
				reference = new PackageResourceReference(Asset.class, "css/common.css");
				response.render(CssReferenceHeaderItem.forReference(reference));
			}
		});
	}

}
