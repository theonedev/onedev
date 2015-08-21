package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare.RequestComparePage;

@SuppressWarnings("serial")
class InlineCommentActivityPanel extends Panel {

	private final IModel<PullRequestComment> commentModel;
	
	public InlineCommentActivityPanel(String id, IModel<PullRequestComment> commentModel) {
		super(id);
		
		this.commentModel = commentModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new CommentPanel("content", commentModel) {

			@Override
			protected Component newActionComponent(String id) {
				Fragment fragment = new Fragment(id, "actionFrag", InlineCommentActivityPanel.this);
				PullRequestComment comment = commentModel.getObject();
				final String path = comment.getInlineInfo().getBlobIdent().path;
				PageParameters params = RequestComparePage.paramsOf(
						comment.getRequest(), comment, null, null, path);
				fragment.add(new BookmarkablePageLink<Void>("fileLink", RequestComparePage.class, params) {

					@Override
					public IModel<?> getBody() {
						return Model.of(path);
					}
					
				});
				
				return fragment;
			}
			
		});
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}
}
