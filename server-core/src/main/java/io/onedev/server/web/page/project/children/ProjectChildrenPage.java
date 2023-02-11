package io.onedev.server.web.page.project.children;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.project.list.ProjectListPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class ProjectChildrenPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private Component projectList;
	
	public ProjectChildrenPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(projectList = new ProjectListPanel("children", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				CharSequence url = RequestCycle.get().urlFor(ProjectChildrenPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}, 0) {

			@Override
			protected Project getParentProject() {
				return getProject();
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getProject(), query, 0);
						params.add(PARAM_PAGE, currentPage+1);
						return params;
					}
					
					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
					}
					
				};
			}

		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(projectList);
	}

	public static PageParameters paramsOf(Project project, @Nullable String query, int page) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Child Projects");
	}
	
	@Override
	protected String getPageTitle() {
		return "Child Projects - " + getProject().getPath();
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		return new ViewStateAwarePageLink<Void>(componentId, ProjectChildrenPage.class, ProjectChildrenPage.paramsOf(project));
	}
	
}
