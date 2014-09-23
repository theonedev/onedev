package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.page.repository.info.pullrequest.RequestComparePage;

@SuppressWarnings("serial")
public class SinceChangesPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final Date sinceDate;
	
	private final String tooltip;
	
	public SinceChangesPanel(String id, IModel<PullRequest> requestModel, Date sinceDate, String tooltip) {
		super(id);
		
		this.requestModel = requestModel;
		this.sinceDate = sinceDate;
		this.tooltip = tooltip;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = requestModel.getObject();
		String oldCommit = request.getBaseCommit();
		for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
			PullRequestUpdate update = request.getSortedUpdates().get(i);
			if (update.getDate().before(sinceDate)) {
				oldCommit = update.getHeadCommit();
				break;
			}
		}
		PageParameters params = RequestComparePage.paramsOf(request, oldCommit, 
				request.getLatestUpdate().getHeadCommit(), null, null);
		
		Link<Void> link = new BookmarkablePageLink<>("link", RequestComparePage.class, params);
		link.add(AttributeAppender.append("title", tooltip));
		add(link);

		setVisible(!oldCommit.equals(request.getLatestUpdate().getHeadCommit()));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

}
