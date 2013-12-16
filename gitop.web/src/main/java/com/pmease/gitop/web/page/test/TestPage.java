package com.pmease.gitop.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	public TestPage() {
		add(new AjaxLink<Void>("test") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				WebMarkupContainer content = new WebMarkupContainer("content");
				content.add(new WebMarkupContainer("modal"));
				System.out.println("hello");
				TestPage.this.replace(content);
				target.add(content);
			}
			
		});
		WebMarkupContainer content = new WebMarkupContainer("content");
		add(content);
		content.setOutputMarkupId(true);
		
		content.add(new ModalPanel("modal") {

			@Override
			protected Component newContent(String id) {
				Fragment frag = new Fragment(id, "frag", TestPage.this);
				frag.add(new ModalPanel("modal") {

					@Override
					protected Component newContent(String id) {
						return new Label(id, "hello");
					}
					
				});
				return frag;
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(BootstrapHeaderItem.get());
	}
	
}
