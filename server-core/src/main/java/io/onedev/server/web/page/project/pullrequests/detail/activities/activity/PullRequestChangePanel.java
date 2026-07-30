package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.SinceChangesLink;

class PullRequestChangePanel extends Panel {

	public PullRequestChangePanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequestChange change = getChange();

		add(new UserIdentPanel("user", change.getUser(), Mode.AVATAR_AND_NAME));
		add(new Label("description", change.getData().getActivity()));

		add(new Label("age", DateUtils.formatAge(change.getDate()))
			.add(new AttributeAppender("data-tippy-content", DateUtils.formatDateTime(change.getDate()))));

		var mergeCommit = new WebMarkupContainer("mergeCommit");
		var mergePreview = change.getRequest().getMergePreview();
		if (change.getData() instanceof PullRequestMergeData
				&& mergePreview != null && mergePreview.getMergeCommitHash() != null) {
			var mergeCommitHash = mergePreview.getMergeCommitHash();
			PageParameters commitParams = CommitDetailPage.paramsOf(
					change.getRequest().getTargetProject(), mergeCommitHash);
			var hashLink = new ViewStateAwarePageLink<Void>(
					"hashLink", CommitDetailPage.class, commitParams);
			hashLink.add(new Label("hash", GitUtils.abbreviateSHA(mergeCommitHash)));
			mergeCommit.add(hashLink);
			mergeCommit.add(new CopyToClipboardLink("copyHash", Model.of(mergeCommitHash)));
		} else {
			mergeCommit.setVisible(false);
		}
		add(mergeCommit);

		add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getChange().getRequest();
			}

		}, getChange().getDate()));
		
		ActivityDetail detail = getChange().getData().getActivityDetail();
		if (detail != null) 
			add(detail.render("body"));
		else 
			add(new WebMarkupContainer("body").setVisible(false));		

		setMarkupId(getChange().getAnchor());
		setOutputMarkupId(true);	
	}

	private PullRequestChange getChange() {
		return ((PullRequestChangeActivity) getDefaultModelObject()).getChange();
	}
	
}
