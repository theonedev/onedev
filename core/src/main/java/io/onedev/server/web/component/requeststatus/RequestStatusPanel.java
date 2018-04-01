package io.onedev.server.web.component.requeststatus;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CloseInfo;

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
				PullRequest request = getPullRequest();
				CloseInfo closeInfo = request.getCloseInfo();
				if (closeInfo == null)
					return "OPEN";
				else 
					return closeInfo.getCloseStatus().toString();
			}
			
		}).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				CloseInfo closeInfo = request.getCloseInfo();
				if (closeInfo == null)
					return "label-warning";
				else if (closeInfo.getCloseStatus() == CloseInfo.Status.MERGED)
					return "label-success";
				else
					return "label-danger";
			}
			
		})));
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
}
