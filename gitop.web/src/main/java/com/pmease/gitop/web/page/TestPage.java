package com.pmease.gitop.web.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.time.Duration;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {
	
	public TestPage() {
		add(new Label("test", "hello world"));
		
		add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this)).hideAfter(Duration.seconds(1)).setOutputMarkupId(true));
		add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				error("error");
				//target.add(TestPage.this.get("feedback"));
			}

		});
	}

}
