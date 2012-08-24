package com.pmease.commons.wicket.asset;

import java.util.Arrays;

import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A stake holder class for asset positioning.
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class Asset extends WebMarkupContainer {

	public Asset(String id) {
		super(id);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		ResourceReference bootstrapJs = new JavaScriptResourceReference(Asset.class, "bootstrap/js/bootstrap.js") {

			@SuppressWarnings("unchecked")
			@Override
			public Iterable<? extends HeaderItem> getDependencies() {
				return Arrays.asList(
						JavaScriptHeaderItem.forReference(WicketEventJQueryResourceReference.get()),
						CssHeaderItem.forReference(new CssResourceReference(Asset.class, "bootstrap/css/bootstrap.css")), 
						CssHeaderItem.forReference(new CssResourceReference(Asset.class, "bootstrap/css/bootstrap-responsive.css")));
			}
			
		};
		
		response.render(JavaScriptHeaderItem.forReference(bootstrapJs));
		
		PackageResourceReference reference = new PackageResourceReference(Asset.class, "js/jquery.align.js");
		response.render(JavaScriptHeaderItem.forReference(reference));
		reference = new PackageResourceReference(Asset.class, "js/common.js");
		response.render(JavaScriptHeaderItem.forReference(reference));
		reference = new PackageResourceReference(Asset.class, "css/common.css");
		response.render(CssReferenceHeaderItem.forReference(reference));
	}

}
