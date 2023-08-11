package io.onedev.server.web.component.pullrequest;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.PullRequest;

@SuppressWarnings("serial")
public class RequestStatusBadge extends Label {

	private final IModel<PullRequest> requestModel;
	
	public RequestStatusBadge(String id, IModel<PullRequest> requestModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return requestModel.getObject().getStatus().toString();
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
				switch (request.getStatus()) {
				case OPEN:
					return "badge badge-warning request-status";
				case MERGED:
					return "badge badge-success request-status";
				default:
					return "badge badge-secondary request-status";
				}
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
