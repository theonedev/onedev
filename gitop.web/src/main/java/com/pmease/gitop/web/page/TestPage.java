package com.pmease.gitop.web.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {
	
	public TestPage() {
		add(new Label("test", "hello world"));
		
		add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this)).setOutputMarkupId(true));
		add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				error("error");
				
				target.add(TestPage.this.get("feedback"));
			}

		});
	}

}
