package com.gitplex.server.web.page.depot.pullrequest.requestdetail;

import java.util.Collection;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.web.component.comment.CodeCommentFilter;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.codecomments.RequestCodeCommentsPage;

@SuppressWarnings("serial")
public class IntegrationHintPanel extends GenericPanel<PullRequest> {

	public IntegrationHintPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getModelObject();
		
		PageParameters params = RequestCodeCommentsPage.paramsOf(request);
		getFilter().fillPageParams(params);
		add(new ViewStateAwarePageLink<Void>("link", RequestCodeCommentsPage.class, params));
	}
	
	private CodeCommentFilter getFilter() {
		CodeCommentFilter filter = new CodeCommentFilter();
		filter.setUnresolved(true);
		return filter;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		Collection<CodeComment> comments = getModelObject().getCodeComments();
		getFilter().filter(comments);
		setVisible(!comments.isEmpty());
	}

}
