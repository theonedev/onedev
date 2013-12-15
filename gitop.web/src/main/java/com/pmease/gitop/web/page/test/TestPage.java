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
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	public TestPage() {
		add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "contentFrag", TestPage.this);
				DropdownPanel dropdown = new DropdownPanel("dropdown") {

					@Override
					protected Component newContent(String id) {
						return new Label(id, "hello world");
					}
					
				};
				fragment.add(dropdown);
				fragment.add(new WebMarkupContainer("trigger").add(new DropdownBehavior(dropdown)));
				TestPage.this.replace(fragment);
				target.add(TestPage.this.get("content"));
			}
			
		});
		
		add(new WebMarkupContainer("content").setOutputMarkupId(true));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(BootstrapHeaderItem.get());
	}
	
}
