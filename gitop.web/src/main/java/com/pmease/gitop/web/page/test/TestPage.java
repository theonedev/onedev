package com.pmease.gitop.web.page.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;

import com.pmease.commons.wicket.behavior.collapse.AccordionPanel;
import com.pmease.commons.wicket.behavior.collapse.CollapseBehavior;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	public TestPage() {
		WebMarkupContainer accordion = new AccordionPanel("accordion");
		add(accordion);
		
		final WebMarkupContainer content1 = new WebMarkupContainer("content1");
		accordion.add(content1);
		accordion.add(new WebMarkupContainer("link1").add(new CollapseBehavior(content1)));

		WebMarkupContainer content2 = new WebMarkupContainer("content2");
		accordion.add(content2);
		accordion.add(new WebMarkupContainer("link2").add(new CollapseBehavior(content2)));

		content1.add(new AjaxLink<Void>("show") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "content", TestPage.this);
				AccordionPanel accordion = new AccordionPanel("accordion");
				fragment.add(accordion);
				WebMarkupContainer content11 = new WebMarkupContainer("content11");
				accordion.add(content11);
				accordion.add(new WebMarkupContainer("link11").add(new CollapseBehavior(content11)));
				
				WebMarkupContainer content12 = new WebMarkupContainer("content12");
				accordion.add(content12);
				accordion.add(new WebMarkupContainer("link12").add(new CollapseBehavior(content12)));
				fragment.setOutputMarkupId(true);
				target.add(fragment);
				content1.replace(fragment);
			}
			
		});
		content1.add(new WebMarkupContainer("content").setOutputMarkupId(true));
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				
			}
			
		});
	}
	
}
