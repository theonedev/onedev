package io.onedev.server.web.editable.job.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.param.ScriptingValues;
import io.onedev.server.ci.job.param.SpecifiedValues;
import io.onedev.server.web.editable.PropertyContext;

@SuppressWarnings("serial")
class ParamListViewPanel extends Panel {

	private final List<JobParam> params = new ArrayList<>();
	
	public ParamListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			params.add((JobParam) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepeatingView paramsView = new RepeatingView("params");
		for (JobParam param: params) {
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
			} else {
				if (param.isSecret())
					paramItem.add(new Label("valuesProvider", ScriptingValues.SECRET_DISPLAY_NAME));
				else
					paramItem.add(new Label("valuesProvider", ScriptingValues.DISPLAY_NAME));
				paramItem.add(PropertyContext.view("values", param.getValuesProvider(), "script"));
			}
			paramsView.add(paramItem);
		}
		add(paramsView);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ParamListCssResourceReference()));
	}
	
}
