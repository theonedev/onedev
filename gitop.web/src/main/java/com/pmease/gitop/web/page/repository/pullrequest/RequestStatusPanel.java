package com.pmease.gitop.web.page.repository.pullrequest;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequest.Status;

@SuppressWarnings("serial")
public class RequestStatusPanel extends Panel {

	public RequestStatusPanel(String id, IModel<PullRequest> requestModel) {
		super(id, requestModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("status", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getStatus().toString();
			}
			
		}).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Status status = getPullRequest().getStatus();
				if (status == Status.DISCARDED)
					return "label-danger";
				else if (status == Status.INTEGRATED)
					return "label-success";
				else 
					return "label-warning";
			}
			
		})));
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
}
