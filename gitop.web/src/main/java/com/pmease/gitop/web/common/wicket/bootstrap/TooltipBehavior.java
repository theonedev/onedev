package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Strings;
import com.pmease.gitop.web.util.JsOptions;

@SuppressWarnings("serial")
public class TooltipBehavior extends Behavior {

	private final IModel<String> titleModel;
	private final IModel<String> placementModel;
	
	public TooltipBehavior() {
		this(null, Model.of("top"));
	}
	
	public TooltipBehavior(IModel<String> titleModel, 
							IModel<String> placementModel) {
		this.titleModel = titleModel;
		this.placementModel = placementModel;
	}

	protected JsOptions getOptions() {
		JsOptions options = new JsOptions();
		if (titleModel != null) {
			options.set("title", titleModel.getObject());
		}
		if (placementModel != null) {
			String placement = placementModel.getObject();
			if (Strings.isNullOrEmpty(placement)) {
				placement = "top";
			}
			options.set("placement", placement);
		}
		
		return options;
	}
	
	protected String getSelector(Component component) {
		return "$('#" + component.getMarkupId(true) + "')";
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("%s.tooltip(%s)", getSelector(component), 
						getOptions().toString())));
	}
	
	@Override
	public void detach(Component component) {
		if (titleModel != null) {
			titleModel.detach();
		}
		
		if (placementModel != null) {
			placementModel.detach();
		}
		
		super.detach(component);
	}
}
