package io.onedev.server.web.page.project.comments;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.codecomment.CodeCommentFilter;
import io.onedev.server.web.component.codecomment.CodeCommentListPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class ProjectCodeCommentsPage extends ProjectPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
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
			
		}, new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject());
				filterOption.fillPageParams(params);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		}));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectCodeCommentsResourceReference()));
	}

}
