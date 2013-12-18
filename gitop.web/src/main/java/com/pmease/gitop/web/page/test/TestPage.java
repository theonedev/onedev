package com.pmease.gitop.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.EnclosureContainer;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	public TestPage() {
		WebMarkupContainer control = new WebMarkupContainer("control");
		control.setVisible(false);
		
		EnclosureContainer container = new EnclosureContainer("container", control);
		add(container);
		container.add(control);
		container.add(new WebMarkupContainer("content") {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				String script = String.format("document.getElementById('%s').className += ' someCssClass';", getMarkupId());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		}.setOutputMarkupId(true));
	}
	
}
