package io.onedev.server.web.component.markdown;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.web.asset.katex.KatexResourceReference;
import io.onedev.server.web.asset.mermaid.MermaidResourceReference;
import io.onedev.server.web.asset.plantuml.PlantUmlResourceReference;

class LazyResourceLoader extends WebMarkupContainer {

	private final IModel<String> renderedModel;
	
	public LazyResourceLoader(String id, IModel<String> renderedModel) {
		super(id);
		this.renderedModel = renderedModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		renderedModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String rendered = renderedModel.getObject();
		if (rendered != null) {
			if (rendered.contains("class=\"mermaid\"")) 
				response.render(JavaScriptHeaderItem.forReference(new MermaidResourceReference()));
			if (rendered.contains("class=\"katex\""))
				response.render(JavaScriptHeaderItem.forReference(new KatexResourceReference()));
			if (rendered.contains("class=\"plantuml\"")) {
				response.render(JavaScriptHeaderItem.forReference(new PlantUmlResourceReference()));
				CharSequence moduleUrl = RequestCycle.get().urlFor(
						new JavaScriptResourceReference(PlantUmlResourceReference.class, "plantuml.js"), null);
				response.render(JavaScriptHeaderItem.forScript(
						"onedev.server.plantumlModuleUrl='" + JavaScriptEscape.escapeJavaScript(moduleUrl.toString()) + "';",
						"onedev-plantuml-module-url"));
			}
		}
	}
	
}
