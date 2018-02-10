package com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.PullRequestManager;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class OpenedActivity implements PullRequestActivity {

	private final Long requestId;
	
	public OpenedActivity(PullRequest request) {
		requestId = request.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new OpenedPanel(panelId, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return getRequest();
			}
			
		});
	}

	public PullRequest getRequest() {
		return TurboDev.getInstance(PullRequestManager.class).load(requestId);
	}
	
	@Override
	public Date getDate() {
		return getRequest().getSubmitDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

}
