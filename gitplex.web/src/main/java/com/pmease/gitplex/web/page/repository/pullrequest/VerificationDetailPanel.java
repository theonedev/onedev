package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.PullRequestVerification;

@SuppressWarnings("serial")
class VerificationDetailPanel extends Panel {

	private IModel<List<PullRequestVerification>> model;
	
	public VerificationDetailPanel(String id, IModel<List<PullRequestVerification>> model) {
		super(id);
		
		this.model = model;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestVerification>("verifications", model) {

			@Override
			protected void populateItem(ListItem<PullRequestVerification> item) {
				PullRequestVerification verification = item.getModelObject();
				item.add(new Label("configuration", verification.getConfiguration()));
				item.add(new Label("message", verification.getMessage()).setEscapeModelStrings(false));
				
				if (verification.getStatus() == PullRequestVerification.Status.NOT_PASSED)
					item.add(AttributeAppender.append("class", "not-passed"));
				else if (verification.getStatus() == PullRequestVerification.Status.PASSED)
					item.add(AttributeAppender.append("class", "passed"));
				else
					item.add(AttributeAppender.append("class", "ongoing"));
			}
			
		});
	}

}
