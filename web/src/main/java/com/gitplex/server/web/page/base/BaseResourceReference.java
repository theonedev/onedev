package com.gitplex.server.web.page.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.gitplex.server.web.assets.js.align.AlignResourceReference;
import com.gitplex.server.web.assets.js.areyousure.AreYouSureResourceReference;
import com.gitplex.server.web.assets.js.autosize.AutoSizeResourceReference;

import de.agilecoders.wicket.core.Bootstrap;

public class BaseResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public BaseResourceReference() {
		super(BaseResourceReference.class, "base.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<HeaderItem>();
		
		dependencies.add(JavaScriptHeaderItem.forReference(Bootstrap.getSettings().getModernizrResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(Bootstrap.getSettings().getJsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AlignResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AutoSizeResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AreYouSureResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseCssResourceReference()));
		
		return dependencies;
	}

}
