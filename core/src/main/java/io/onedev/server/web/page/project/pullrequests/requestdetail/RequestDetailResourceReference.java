package io.onedev.server.web.page.project.pullrequests.requestdetail;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.perfectscrollbar.PerfectScrollbarResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class RequestDetailResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public RequestDetailResourceReference() {
		super(RequestDetailResourceReference.class, "request-detail.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new PerfectScrollbarResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				RequestDetailResourceReference.class, "request-detail.css")));
		return dependencies;
	}

}
