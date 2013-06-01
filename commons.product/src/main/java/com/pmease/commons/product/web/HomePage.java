package com.pmease.commons.product.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;

import com.pmease.commons.wicket.decorator.ajaxloadingindicator.AjaxLoadingIndicator;
import com.pmease.commons.wicket.decorator.ajaxloadingoverlay.AjaxLoadingOverlay;

@SuppressWarnings("serial")
public class HomePage extends WebPage {
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("update") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new AjaxLoadingOverlay());
				attributes.getAjaxCallListeners().add(new AjaxLoadingIndicator());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			
		});
		
		add(new AjaxLink<Void>("update2") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new AjaxLoadingOverlay());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			
		});
	}
	
}