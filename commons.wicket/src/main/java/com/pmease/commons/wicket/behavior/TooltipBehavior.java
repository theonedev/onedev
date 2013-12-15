package com.pmease.commons.wicket.behavior;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

public class TooltipBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	private final IModel<String> contentModel;
	
	public TooltipBehavior(IModel<String> contentModel) {
		this.contentModel = contentModel;
	}
	
	public TooltipBehavior(String content) {
		this(Model.of(content));
	}
	
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(BootstrapHeaderItem.get());
		String title = StringEscapeUtils.escapeEcmaScript(contentModel.getObject());
		String script = String.format("$('#%s').tooltip({title: '%s', placement: 'auto top'})", 
				component.getMarkupId(), title);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	public void detach(Component component) {
		contentModel.detach();
		super.detach(component);
	}

}
