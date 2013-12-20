package com.pmease.commons.wicket.behavior;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.jackson.JsOptions;
import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

public class TooltipBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	private final IModel<String> titleModel;
	
	private final String selector;
	
	private final String placement;
	
	public TooltipBehavior(@Nullable String selector, @Nullable IModel<String> titleModel, @Nullable String placement) {
		this.selector = selector;
		this.titleModel = titleModel;
		this.placement = placement;
	}
	
	public TooltipBehavior(@Nullable IModel<String> titleModel) {
		this(null, titleModel, null);
	}
	
	public TooltipBehavior() {
		this(null);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(BootstrapHeaderItem.get());
		JsOptions options = new JsOptions();
		if (titleModel != null)
			options.put("title", StringEscapeUtils.escapeEcmaScript(titleModel.getObject()));
		if (placement != null) {
			options.put("placement", placement);
		} else {
			options.put("placement", "top auto");
		}
		
		String script;
		if (selector != null)
			script = String.format("$('#%s %s').tooltip(%s)", component.getMarkupId(true), selector, options.toString());
		else
			script = String.format("$('#%s').tooltip(%s)", component.getMarkupId(true), options.toString());
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	public void detach(Component component) {
		if (titleModel != null)
			titleModel.detach();
		super.detach(component);
	}

}
