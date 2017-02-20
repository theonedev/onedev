package com.gitplex.server.web.component.pullrequest.requeststatus;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequest.Status;
import com.gitplex.server.model.support.CloseInfo;

@SuppressWarnings("serial")
public class RequestStatusPanel extends Panel {

	private final boolean checkGateKeeper;
	
	public RequestStatusPanel(String id, IModel<PullRequest> requestModel, boolean checkGateKeeper) {
		super(id, requestModel);
		
		this.checkGateKeeper = checkGateKeeper;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("status", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (checkGateKeeper) {
					return request.getStatus().toString();
				} else {
					CloseInfo closeInfo = request.getCloseInfo();
					if (closeInfo == null)
						return "OPEN";
					else 
						return closeInfo.getCloseStatus().toString();
				}
			}
			
		}).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (checkGateKeeper) {
					Status status = request.getStatus();
					if (status == Status.DISCARDED)
						return "label-danger";
					else if (status == Status.INTEGRATED)
						return "label-success";
					else 
						return "label-warning";
				} else {
					CloseInfo closeInfo = request.getCloseInfo();
					if (closeInfo == null)
						return "label-warning";
					else if (closeInfo.getCloseStatus() == CloseInfo.Status.INTEGRATED)
						return "label-success";
					else
						return "label-danger";
				}
			}
			
		})));
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
}
