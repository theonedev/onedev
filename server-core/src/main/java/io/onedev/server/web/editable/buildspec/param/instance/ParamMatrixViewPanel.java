package io.onedev.server.web.editable.buildspec.param.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import io.onedev.server.buildspec.param.instance.IgnoreValues;
import io.onedev.server.buildspec.param.instance.ParamInstances;
import io.onedev.server.buildspec.param.instance.PassthroughValues;
import io.onedev.server.buildspec.param.instance.ScriptingValues;
import io.onedev.server.buildspec.param.instance.SpecifiedValues;
import io.onedev.server.web.editable.PropertyContext;

class ParamMatrixViewPanel extends Panel {

	private final List<ParamInstances> params = new ArrayList<>();
	
	public ParamMatrixViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			params.add((ParamInstances) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepeatingView paramsView = new RepeatingView("params");
		for (ParamInstances param: params) {
			WebMarkupContainer paramItem = new WebMarkupContainer(paramsView.newChildId());
			paramItem.add(new Label("name", param.getName()));
			
			if (param.getValuesProvider() instanceof SpecifiedValues) {
				if (param.isSecret())
					paramItem.add(new Label("valuesProvider", SpecifiedValues.SECRET_DISPLAY_NAME));
				else
					paramItem.add(new Label("valuesProvider", SpecifiedValues.DISPLAY_NAME));
				Fragment fragment = new Fragment("values", "specifiedValuesFrag", this);
				RepeatingView valuesView = new RepeatingView("values");
				SpecifiedValues specifiedValues = (SpecifiedValues) param.getValuesProvider();
				for (List<String> value: specifiedValues.getValues()) {
					WebMarkupContainer valueItem = new WebMarkupContainer(valuesView.newChildId());
					if (value.size() == 0) 
						valueItem.add(new Label("value", "<i>Empty</i>").setEscapeModelStrings(false));
					else if (value.size() == 1)
						valueItem.add(new Label("value", value.iterator().next()));
					else 
						valueItem.add(new Label("value", value.toString()));
					valuesView.add(valueItem);
				}
				fragment.add(valuesView);
				paramItem.add(fragment);
			} else if (param.getValuesProvider() instanceof ScriptingValues) {
				if (param.isSecret())
					paramItem.add(new Label("valuesProvider", ScriptingValues.SECRET_DISPLAY_NAME));
				else
					paramItem.add(new Label("valuesProvider", ScriptingValues.DISPLAY_NAME));
				paramItem.add(PropertyContext.view("values", param.getValuesProvider(), "scriptName"));
			} else if (param.getValuesProvider() instanceof PassthroughValues) {
				paramItem.add(new Label("valuesProvider", PassthroughValues.DISPLAY_NAME));
				paramItem.add(PropertyContext.view("values", param.getValuesProvider(), "paramName"));
			} else {
				paramItem.add(new Label("valuesProvider", IgnoreValues.DISPLAY_NAME));
				paramItem.add(new WebMarkupContainer("values"));
			}
			paramsView.add(paramItem);
		}
		add(paramsView);
	}
	
}
