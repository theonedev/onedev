package io.onedev.server.web.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CloseInfo;

@SuppressWarnings("serial")
public class RequestStatusLabel extends Label {

	private final IModel<PullRequest> requestModel;
	
	public RequestStatusLabel(String id, IModel<PullRequest> requestModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequest request = requestModel.getObject();
				CloseInfo closeInfo = request.getCloseInfo();
				if (closeInfo == null)
					return "OPEN";
				else 
					return closeInfo.getCloseStatus().toString();
			}
			
		});
		this.requestModel = requestModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				CloseInfo closeInfo = request.getCloseInfo();
				if (closeInfo == null)
					return "label label-warning request-status";
				else if (closeInfo.getCloseStatus() == CloseInfo.Status.MERGED)
					return "label label-success request-status";
				else
					return "label label-danger request-status";
			}
			
		}));
	}

	private PullRequest getPullRequest() {
		return requestModel.getObject();
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}
	
}
