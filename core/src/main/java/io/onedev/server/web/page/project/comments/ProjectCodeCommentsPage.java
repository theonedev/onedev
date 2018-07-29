package io.onedev.server.web.page.project.comments;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.codecomment.CodeCommentListPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class ProjectCodeCommentsPage extends ProjectPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private final String query;
	
	public ProjectCodeCommentsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), query);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(new CodeCommentListPanel("codeComments", query) {

			@Override
			protected Project getProject() {
				return ProjectCodeCommentsPage.this.getProject();
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target) {
				PageParameters params = paramsOf(getProject(), getQuery());
				setResponsePage(ProjectCodeCommentsPage.class, params);
			}

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

	public static PageParameters paramsOf(Project project, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
