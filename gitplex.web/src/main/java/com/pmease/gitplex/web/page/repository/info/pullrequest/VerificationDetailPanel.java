package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Verification;

@SuppressWarnings("serial")
public class VerificationDetailPanel extends Panel {

	private IModel<List<Verification>> model;
	
	public VerificationDetailPanel(String id, IModel<List<Verification>> model) {
		super(id);
		
		this.model = model;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Verification>("verifications", model) {

			@Override
			protected void populateItem(ListItem<Verification> item) {
				Verification verification = item.getModelObject();
				item.add(new Label("configuration", verification.getConfiguration()));
				item.add(new Label("message", verification.getMessage()).setEscapeModelStrings(false));
				
				if (verification.getStatus() == Verification.Status.NOT_PASSED)
					item.add(AttributeAppender.append("class", "not-passed"));
				else if (verification.getStatus() == Verification.Status.PASSED)
					item.add(AttributeAppender.append("class", "passed"));
				else
					item.add(AttributeAppender.append("class", "ongoing"));
			}
			
		});
	}

}
