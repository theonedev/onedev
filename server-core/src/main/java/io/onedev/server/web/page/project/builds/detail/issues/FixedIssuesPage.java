package io.onedev.server.web.page.project.builds.detail.issues;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.FixedInBuildCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class FixedIssuesPage extends BuildDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";

	private String query;
	
	private IssueListPanel issueList;
	
	public FixedIssuesPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(issueList = new IssueListPanel("issues", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(FixedIssuesPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

			@Override
			protected IssueQuery getBaseQuery() {
				return new IssueQuery(new FixedInBuildCriteria(getBuild()), new ArrayList<>());
			}
			
			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getBuild(), query);
						params.add(PARAM_PAGE, currentPage+1);
						return params;
					}
					
					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
					}
					
				};
			}

			@Override
			protected Project getProject() {
				return FixedIssuesPage.this.getProject();
			}
			
		});
		
		add(new WebMarkupContainer("buildStreamHelpUrl") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("href", OneDev.getInstance().getDocRoot() + "/pages/concepts.md#build-stream");
			}
			
		});
		
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(issueList);
	}

	public static PageParameters paramsOf(Build build, @Nullable String query) {
		PageParameters params = paramsOf(build);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
