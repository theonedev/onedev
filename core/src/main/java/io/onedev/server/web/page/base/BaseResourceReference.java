package io.onedev.server.web.page.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import de.agilecoders.wicket.core.Bootstrap;
import io.onedev.server.web.asset.align.AlignResourceReference;
import io.onedev.server.web.asset.autosize.AutoSizeResourceReference;
import io.onedev.server.web.asset.cookies.CookiesResourceReference;
import io.onedev.server.web.asset.perfectscrollbar.PerfectScrollbarResourceReference;
import io.onedev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;

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
		dependencies.add(JavaScriptHeaderItem.forReference(new PerfectScrollbarResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		
		dependencies.add(CssHeaderItem.forReference(new BaseCssResourceReference()));
		
		return dependencies;
	}

}
