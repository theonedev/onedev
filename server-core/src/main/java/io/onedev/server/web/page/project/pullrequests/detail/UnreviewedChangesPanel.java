package io.onedev.server.web.page.project.pullrequests.detail;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;

@SuppressWarnings("serial")
public class UnreviewedChangesPanel extends GenericPanel<PullRequestUpdate> {

	public UnreviewedChangesPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequestUpdate update = getModelObject();

		PullRequestChangesPage.State state = new PullRequestChangesPage.State();
		state.oldCommitHash = update.getHeadCommitHash();
		state.newCommitHash = update.getRequest().getLatestUpdate().getHeadCommitHash();
		
		add(new ViewStateAwarePageLink<Void>("link", PullRequestChangesPage.class, 
				PullRequestChangesPage.paramsOf(update.getRequest(), state)));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		PullRequestUpdate update = getModelObject();
		setVisible(!update.equals(update.getRequest().getLatestUpdate()));
	}
	
}
