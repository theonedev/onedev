package com.gitplex.server.web.component.comment.comparecontext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.DepotAndRevision;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.web.component.revisionpicker.RevisionSelector;
import com.gitplex.server.web.page.depot.compare.RevisionComparePage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;

@SuppressWarnings("serial")
public class CompareContextPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<CodeComment> commentModel;
	
	private final IModel<WhitespaceOption> whitespaceOptionModel;
	
	public CompareContextPanel(String id, IModel<PullRequest> requestModel, IModel<CodeComment> commentModel, 
			IModel<WhitespaceOption> whitespaceOptionModel) {
		super(id);
		this.requestModel = requestModel;
		this.commentModel = commentModel;
		this.whitespaceOptionModel = whitespaceOptionModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (requestModel.getObject() != null) {
			Fragment fragment = new Fragment("content", "requestFrag", this);
			add(fragment);
			
			IModel<List<RevCommit>> commitsModel = new LoadableDetachableModel<List<RevCommit>>() {
	
				@Override
				protected List<RevCommit> load() {
					List<RevCommit> commits = new ArrayList<>();
					PullRequest request = requestModel.getObject();
					commits.add(request.getBaseCommit());
					commits.addAll(request.getCommits());
					return commits;
				}
				
			};
			
			fragment.add(new ListView<RevCommit>("commits", commitsModel) {
	
				@Override
				protected void populateItem(ListItem<RevCommit> item) {
					RevCommit commit = item.getModelObject();
					AjaxLink<Void> link = new AjaxLink<Void>("link") {
	
						@Override
						public void onClick(AjaxRequestTarget target) {
							RequestChangesPage.State state = new RequestChangesPage.State();
							CodeComment comment = commentModel.getObject();
							state.commentId = comment.getId();
							state.mark = comment.getCommentPos();
							int index = commitsModel.getObject().stream().map(RevCommit::getName).collect(Collectors.toList())
									.indexOf(comment.getCommentPos().getCommit());
							int compareIndex = commitsModel.getObject().indexOf(commit);
							if (index < compareIndex) {
								state.oldCommit = comment.getCommentPos().getCommit();
								state.newCommit = commit.name();
							} else {
								state.oldCommit = commit.name();
								state.newCommit = comment.getCommentPos().getCommit();
							}
							state.whitespaceOption = whitespaceOptionModel.getObject();
							PageParameters params = RequestChangesPage.paramsOf(requestModel.getObject(), state);
							setResponsePage(RequestChangesPage.class, params);
						}
						
					};
					link.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
					link.add(new Label("subject", commit.getShortMessage()));
					if (commit.name().equals(commentModel.getObject().getCommentPos().getCommit())) {
						link.setEnabled(false);
						link.add(AttributeAppender.append("class", "commented"));
						link.add(new WebMarkupContainer("commented"));
					} else {
						link.add(new WebMarkupContainer("commented").setVisible(false));
					}
					item.add(link);
				}
				
			});
		} else {
			add(new RevisionSelector("content", new AbstractReadOnlyModel<Depot>() {

				@Override
				public Depot getObject() {
					return commentModel.getObject().getDepot();
				}
				
			}) {
				
				@Override
				protected void onSelect(AjaxRequestTarget target, String revision) {
					RevisionComparePage.State state = new RevisionComparePage.State();
					CodeComment comment = commentModel.getObject();
					state.commentId = comment.getId();
					state.mark = comment.getCommentPos();
					state.compareWithMergeBase = false;
					state.leftSide = new DepotAndRevision(comment.getDepot(), comment.getCommentPos().getCommit());
					state.rightSide = new DepotAndRevision(comment.getDepot(), revision);
					state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
					PageParameters params = RevisionComparePage.paramsOf(comment.getDepot(), state);
					setResponsePage(RevisionComparePage.class, params);
				}
				
			});
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CompareContextResourceReference()));
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		requestModel.detach();
		whitespaceOptionModel.detach();
		
		super.onDetach();
	}

}
