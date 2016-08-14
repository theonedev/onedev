package com.pmease.gitplex.web.page.depot.comments;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;

@SuppressWarnings("serial")
public class CodeCommentPage extends DepotPage {

	private static final String PARAM_COMMENT = "comment";

	private final IModel<CodeComment> commentModel;
	
	public CodeCommentPage(PageParameters params) {
		super(params);

		Long commentId = params.get(PARAM_COMMENT).toLong();
		commentModel = new LoadableDetachableModel<CodeComment>() {

			@Override
			protected CodeComment load() {
				return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
			}
			
		};
		
		CodeComment comment = commentModel.getObject();
		if (comment.getRelations().isEmpty()) {
			CompareContext compareContext = comment.getLastCompareContext();
			if (!compareContext.getCompareCommit().equals(comment.getCommentPos().getCommit())) {
				throw new RestartResponseException(
						RevisionComparePage.class, 
						RevisionComparePage.paramsOf(getDepot(), comment));
			} else {
				throw new RestartResponseException(
						DepotFilePage.class, 
						DepotFilePage.paramsOf(getDepot(), comment));
			}
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequest>("requests", new LoadableDetachableModel<List<PullRequest>>() {

			@Override
			protected List<PullRequest> load() {
				return commentModel.getObject().getRelations()
						.stream()
						.map(CodeCommentRelation::getRequest)
						.sorted((o1, o2)->(int)(o2.getId()-o1.getId()))
						.collect(Collectors.toList());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<PullRequest> item) {
				PullRequest request = item.getModelObject();
				CodeComment comment = commentModel.getObject();
				PageParameters params = RequestChangesPage.paramsOf(request, comment);
				Link<Void> link = new BookmarkablePageLink<Void>("request", 
						RequestChangesPage.class, params);
				link.add(new Label("number", request.getNumber()));
				link.add(new Label("title", request.getTitle()));
				item.add(link);
			}
			
		});

		CodeComment comment = commentModel.getObject();
		CompareContext compareContext = comment.getLastCompareContext();
		if (!compareContext.getCompareCommit().equals(comment.getCommentPos().getCommit())) {
			add(new BookmarkablePageLink<Void>("link", 
					RevisionComparePage.class, 
					RevisionComparePage.paramsOf(getDepot(), comment)));
		} else {
			add(new BookmarkablePageLink<Void>("link", 
					DepotFilePage.class, 
					DepotFilePage.paramsOf(getDepot(), comment)));
		}
	}

	public static PageParameters paramsOf(Depot depot, CodeComment comment) {
		PageParameters params = paramsOf(depot);
		params.add(PARAM_COMMENT, comment.getId());
		return params;
	}
	
	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(CodeCommentPage.class, "depot-comments.css")));
	}

}
