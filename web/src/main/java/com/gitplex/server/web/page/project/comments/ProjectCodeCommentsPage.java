package com.gitplex.server.web.page.project.comments;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.web.component.comment.CodeCommentFilter;
import com.gitplex.server.web.component.comment.CodeCommentListPanel;
import com.gitplex.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class ProjectCodeCommentsPage extends ProjectPage {

	private final CodeCommentFilter filterOption;
	
	public ProjectCodeCommentsPage(PageParameters params) {
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
				PageParameters params = paramsOf(getProject());
				object.fillPageParams(params);
				setResponsePage(ProjectCodeCommentsPage.class, params);
			}
			
		}) {
			
			@Override
			protected PullRequest getPullRequest() {
				return null;
			}

		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectCodeCommentsResourceReference()));
	}

}
