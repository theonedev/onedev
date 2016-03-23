package com.pmease.gitplex.web.page.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.pmease.commons.wicket.ajaxlistener.IndicateWorkingListener;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("test") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new IndicateWorkingListener(true));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}

}
