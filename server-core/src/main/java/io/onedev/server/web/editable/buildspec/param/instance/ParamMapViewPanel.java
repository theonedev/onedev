package io.onedev.server.web.editable.buildspec.param.instance;

import io.onedev.server.buildspec.param.instance.*;
import io.onedev.server.web.editable.PropertyContext;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class ParamMapViewPanel extends Panel {

	private final List<ParamInstance> params = new ArrayList<>();
	
	public ParamMapViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			params.add((ParamInstance) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepeatingView paramsView = new RepeatingView("params");
		for (ParamInstance param: params) {
			WebMarkupContainer paramItem = new WebMarkupContainer(paramsView.newChildId());
			paramItem.add(new Label("name", param.getName()));
			
			if (param.getValueProvider() instanceof SpecifiedValue) {
				if (param.isSecret())
					paramItem.add(new Label("valueProvider", SpecifiedValue.SECRET_DISPLAY_NAME));
				else
					paramItem.add(new Label("valueProvider", SpecifiedValue.DISPLAY_NAME));
				var value = ((SpecifiedValue) param.getValueProvider()).getValue();
				if (value.size() == 0)
					paramItem.add(new Label("value", "<i>Empty</i>").setEscapeModelStrings(false));
				else if (value.size() == 1)
					paramItem.add(new Label("value", value.iterator().next()));
				else
					paramItem.add(new Label("value", value.toString()));
			} else if (param.getValueProvider() instanceof ScriptingValue) {
				if (param.isSecret())
					paramItem.add(new Label("valueProvider", ScriptingValue.SECRET_DISPLAY_NAME));
				else
					paramItem.add(new Label("valueProvider", ScriptingValue.DISPLAY_NAME));
				paramItem.add(PropertyContext.view("value", param.getValueProvider(), "scriptName"));
			} else if (param.getValueProvider() instanceof PassthroughValue) {
				paramItem.add(new Label("valueProvider", PassthroughValue.DISPLAY_NAME));
				paramItem.add(PropertyContext.view("value", param.getValueProvider(), "paramName"));
			} else {
				paramItem.add(new Label("valueProvider", IgnoreValue.DISPLAY_NAME));
				paramItem.add(new WebMarkupContainer("value"));
			}
			paramsView.add(paramItem);
		}
		add(paramsView);
	}
	
}
