package io.onedev.server.web.component.pullrequest;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.PullRequest;

public class RequestStatusBadge extends Label {

	private final IModel<PullRequest> requestModel;
	
	public RequestStatusBadge(String id, IModel<PullRequest> requestModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return _T(requestModel.getObject().getStatus().toString());
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
					return "badge badge-info request-status";
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
