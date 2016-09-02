package com.pmease.commons.wicket.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.align.AlignResourceReference;
import com.pmease.commons.wicket.assets.areyousure.AreYouSureResourceReference;
import com.pmease.commons.wicket.assets.autosize.AutoSizeResourceReference;

import de.agilecoders.wicket.core.Bootstrap;

/**
 * Common resource is intended to provide a common look&feel for all products using 
 * commons.wicket 
 * 
 * @author robin
 *
 */
public class CommonResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public CommonResourceReference() {
		super(CommonResourceReference.class, "common.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<HeaderItem>();
		
		dependencies.add(JavaScriptHeaderItem.forReference(Bootstrap.getSettings().getModernizrResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(Bootstrap.getSettings().getJsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AlignResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AutoSizeResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AreYouSureResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CommonCssResourceReference()));

		return dependencies;
	}

}
