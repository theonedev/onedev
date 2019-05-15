package io.onedev.server.web.editable.job.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
	
	public ParamListViewPanel(String id, Class<?> elementClass, List<Serializable> elements) {
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
				paramItem.add(new Label("valuesProvider", SpecifiedValues.DISPLAY_NAME));
				Fragment fragment = new Fragment("values", "specifiedValuesFrag", this);
				RepeatingView valuesView = new RepeatingView("values");
				SpecifiedValues specifiedValues = (SpecifiedValues) param.getValuesProvider();
				for (String value: specifiedValues.getValues()) {
					WebMarkupContainer valueItem = new WebMarkupContainer(valuesView.newChildId());
					if (StringUtils.isNotBlank(value))
						valueItem.add(new Label("value", value));
					else
						valueItem.add(new Label("value", "<i>Empty value</i>").setEscapeModelStrings(false));
					valuesView.add(valueItem);
				}
				fragment.add(valuesView);
				paramItem.add(fragment);
			} else {
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
