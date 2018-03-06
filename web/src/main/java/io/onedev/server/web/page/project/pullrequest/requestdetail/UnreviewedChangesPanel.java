package io.onedev.server.web.page.project.pullrequest.requestdetail;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequest.requestdetail.changes.RequestChangesPage;

@SuppressWarnings("serial")
public class UnreviewedChangesPanel extends GenericPanel<PullRequestUpdate> {

	public UnreviewedChangesPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequestUpdate update = getModelObject();

		RequestChangesPage.State state = new RequestChangesPage.State();
		state.oldCommit = update.getHeadCommitHash();
		state.newCommit = update.getRequest().getHeadCommitHash();
		
		add(new ViewStateAwarePageLink<Void>("link", RequestChangesPage.class, 
				RequestChangesPage.paramsOf(update.getRequest(), state)));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		PullRequestUpdate update = getModelObject();
		setVisible(!update.equals(update.getRequest().getLatestUpdate()));
	}
	
}
