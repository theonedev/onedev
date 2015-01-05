package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.event.PullRequestChanged;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestComparePage;

@SuppressWarnings("serial")
public class SinceChangesLink extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<String> oldCommitModel = new LoadableDetachableModel<String>() {

		@Override
		protected String load() {
			PullRequest request = requestModel.getObject();
			String oldCommit = request.getBaseCommitHash();
			for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				if (update.getDate().before(sinceDate)) {
					oldCommit = update.getHeadCommitHash();
					break;
				}
			}
			return oldCommit;
		}
		
	};
	
	private final Date sinceDate;
	
	private final String tooltip;
	
	public SinceChangesLink(String id, IModel<PullRequest> requestModel, Date sinceDate, String tooltip) {
		super(id);
		
		this.requestModel = requestModel;
		this.sinceDate = sinceDate;
		this.tooltip = tooltip;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<Void> link = new Link<Void>("link") {

			@Override
			public void onClick() {
				PullRequest request = requestModel.getObject();
				PageParameters params = RequestComparePage.paramsOf(request, oldCommitModel.getObject(), 
						request.getLatestUpdate().getHeadCommitHash(), null);
				setResponsePage(RequestComparePage.class, params);
			}
			
		};
		link.add(AttributeAppender.append("title", tooltip));
		add(link);
		
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PullRequestChanged) {
			PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
			AjaxRequestTarget target = pullRequestChanged.getTarget();
			target.add(this);
		}
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!oldCommitModel.getObject().equals(requestModel.getObject().getLatestUpdate().getHeadCommitHash()));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		oldCommitModel.detach();
		
		super.onDetach();
	}

}
