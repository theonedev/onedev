package io.onedev.server.web.component.issue.pullrequests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.infomanager.CodeCommentRelationInfoManager;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.pullrequest.RequestStatusLabel;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

@SuppressWarnings("serial")
public class IssuePullRequestsPanel extends GenericPanel<Issue> {

	private final IModel<List<PullRequest>> requestsModel = new LoadableDetachableModel<List<PullRequest>>() {

		@Override
		protected List<PullRequest> load() {
			List<PullRequest> requests = new ArrayList<>();
			
			CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class); 
			Collection<ObjectId> fixCommits = commitInfoManager.getFixCommits(getIssue().getProject(), getIssue().getNumber());
			CodeCommentRelationInfoManager codeCommentRelationInfoManager = OneDev.getInstance(CodeCommentRelationInfoManager.class); 
			Collection<Long> pullRequestIds = new HashSet<>();
			for (ObjectId commit: fixCommits) 
				pullRequestIds.addAll(codeCommentRelationInfoManager.getPullRequestIds(getIssue().getProject(), commit));		
			
			for (Long requestId: pullRequestIds) {
				PullRequest request = OneDev.getInstance(PullRequestManager.class).get(requestId);
				if (request != null && !requests.contains(request))
					requests.add(request);
			}
			Collections.sort(requests, new Comparator<PullRequest>() {

				@Override
				public int compare(PullRequest o1, PullRequest o2) {
					return o2.getId().compareTo(o1.getId());
				}
				
			});
			return requests;
		}
		
	};
	
	public IssuePullRequestsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequest>("pullRequests", requestsModel) {

			@Override
			protected void populateItem(ListItem<PullRequest> item) {
				PullRequest request = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>("title", PullRequestActivitiesPage.class, 
						PullRequestActivitiesPage.paramsOf(request, null));
				link.add(new Label("label",  "#" + request.getNumber() + " - " + request.getTitle()));
				link.add(AttributeAppender.append("title", request.getTitle()));
				item.add(link);
				item.add(new RequestStatusLabel("status", item.getModel()));
			}}
		
		);
	}

	private Issue getIssue() {
		return getModelObject();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canReadCode(getIssue().getProject()) 
				&& !requestsModel.getObject().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssuePullRequestsCssResourceReference()));
	}

	@Override
	protected void onDetach() {
		requestsModel.detach();
		super.onDetach();
	}
	
}
