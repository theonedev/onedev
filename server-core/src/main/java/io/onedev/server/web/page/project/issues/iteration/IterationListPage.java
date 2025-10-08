package io.onedev.server.web.page.project.issues.iteration;

import static io.onedev.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.util.IterationSort;
import io.onedev.server.web.component.iteration.list.IterationListPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;

public class IterationListPage extends ProjectPage {

	private static final String PARAM_STATE = "state";
	
	private static final String PARAM_SORT = "sort";
	
	private static final String PARAM_PAGE = "page";
	
	private final boolean closed;
	
	private final IterationSort sort;
	
	public IterationListPage(PageParameters params) {
		super(params);
		
		String state = params.get(PARAM_STATE).toString();
		if (state == null)
			closed = false;
		else 
			closed = state.toLowerCase().equals("closed");
			
		String sortString = params.get(PARAM_SORT).toString();
		if (sortString != null)
			sort = IterationSort.valueOf(sortString.toUpperCase());
		else
			sort = IterationSort.CLOSEST_DUE_DATE;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PagingHistorySupport pagingHistorySupport = new ParamPagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), closed, sort);
				params.add(PARAM_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		add(new IterationListPanel("iterations", projectModel, closed, sort, pagingHistorySupport) {

			@Override
			protected void onSortChanged(AjaxRequestTarget target, IterationSort sort) {
				setResponsePage(IterationListPage.class, paramsOf(getProject(), closed, sort));
			}

			@Override
			protected void onStateChanged(AjaxRequestTarget target, boolean closed) {
				setResponsePage(IterationListPage.class, paramsOf(getProject(), closed, sort));
			}
			
		});
	}

	public static PageParameters paramsOf(Project project, boolean closed, @Nullable IterationSort sort) {
		PageParameters params = paramsOf(project);
		if (closed)
			params.add(PARAM_STATE, "closed");
		else
			params.add(PARAM_STATE, "open");
			
		if (sort != null)
			params.add(PARAM_SORT, sort.name().toLowerCase());
		return params;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Iterations"));
	}

	@Override
	protected String getPageTitle() {
		return _T("Iterations") + " - " + getProject().getPath();
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, IterationListPage.class, IterationListPage.paramsOf(project, false, null));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
