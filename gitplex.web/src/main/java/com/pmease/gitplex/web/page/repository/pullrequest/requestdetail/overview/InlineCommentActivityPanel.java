package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare.RequestComparePage;

@SuppressWarnings("serial")
class InlineCommentActivityPanel extends Panel {

	private final IModel<Comment> commentModel;
	
	public InlineCommentActivityPanel(String id, IModel<Comment> commentModel) {
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
				fragment.add(new Link<Void>("fileLink") {

					@Override
					public void onClick() {
						PageParameters params;
						Comment comment = commentModel.getObject();
						GitPlex.getInstance(CommentManager.class).updateInlineInfo(comment);
						if (comment.getBlobIdent().equals(comment.getCompareWith())) {
							params = RepoFilePage.paramsOf(comment);
							setResponsePage(RepoFilePage.class, params);
						} else {
							params = RequestComparePage.paramsOf(comment);
							setResponsePage(RequestComparePage.class, params);
						}
					}
					
					@Override
					public IModel<?> getBody() {
						return Model.of(commentModel.getObject().getInlineInfo().getBlobIdent().path);
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
