package com.pmease.gitop.web.page.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

import com.pmease.commons.wicket.behavior.ConfirmBehavior;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	public TestPage() {
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		container.setOutputMarkupId(true);
		container.add(new WebMarkupContainer("test"));
		
		add(new AjaxLink<Void>("show") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				WebMarkupContainer container = new WebMarkupContainer("container");
				TestPage.this.replace(container);
				container.add(new AjaxLink<Void>("test") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						System.out.println("hello");
					}
					
				}.add(new ConfirmBehavior("really?")));
				target.add(container);
			}
			
		});
	}
	
}
