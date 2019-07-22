package io.onedev.server.web.asset.echarts;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.resourcebundle.ResourceBundle;

@ResourceBundle
public class EChartsResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public EChartsResourceReference() {
		super(EChartsResourceReference.class, "echarts.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		return dependencies;
	}
	
}
