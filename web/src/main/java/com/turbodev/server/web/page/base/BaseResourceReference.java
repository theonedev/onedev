package com.turbodev.server.web.page.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.turbodev.server.web.asset.align.AlignResourceReference;
import com.turbodev.server.web.asset.areyousure.AreYouSureResourceReference;
import com.turbodev.server.web.asset.autosize.AutoSizeResourceReference;
import com.turbodev.server.web.asset.cookies.CookiesResourceReference;
import com.turbodev.server.web.asset.perfectscrollbar.PerfectScrollbarResourceReference;

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
		dependencies.add(JavaScriptHeaderItem.forReference(new PerfectScrollbarResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		
		dependencies.add(CssHeaderItem.forReference(new BaseCssResourceReference()));
		
		return dependencies;
	}

}
