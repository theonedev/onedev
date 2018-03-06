package io.onedev.server.web.page.project.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.page.project.pullrequest.requestdetail.overview.PullRequestActivity;

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
		return OneDev.getInstance(PullRequestManager.class).load(requestId);
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
