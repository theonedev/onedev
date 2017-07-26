package com.gitplex.server.web.page.project.pullrequest.requestdetail.codecomments;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.web.component.comment.CodeCommentFilter;
import com.gitplex.server.web.component.comment.CodeCommentListPanel;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.RequestDetailPage;

@SuppressWarnings("serial")
public class RequestCodeCommentsPage extends RequestDetailPage {

	private final CodeCommentFilter filterOption;
	
	public RequestCodeCommentsPage(PageParameters params) {
		super(params);

		filterOption = new CodeCommentFilter(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new CodeCommentListPanel("codeComments", new IModel<CodeCommentFilter>() {

			@Override
			public void detach() {
			}

			@Override
			public CodeCommentFilter getObject() {
				return filterOption;
			}

			@Override
			public void setObject(CodeCommentFilter object) {
				PageParameters params = paramsOf(getPullRequest());
				object.fillPageParams(params);
				setResponsePage(RequestCodeCommentsPage.class, params);
			}
			
		}) {
			
			@Override
			protected PullRequest getPullRequest() {
				return RequestCodeCommentsPage.this.getPullRequest();
			}

		});
	}

}
