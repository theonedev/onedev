package com.pmease.commons.wicket.component.backtotop;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class BackToTop extends Panel {

	public BackToTop(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("backToTop") {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript(
						String.format("pmease.commons.backToTop('#%s');", getMarkupId())));
			}
			
		}.setOutputMarkupId(true));
	}

}
