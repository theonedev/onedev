package io.onedev.server.web.page.project.issues.milestones;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.MilestoneCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.MilestoneDueLabel;
import io.onedev.server.web.component.MilestoneStatusLabel;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.issuelist.IssueListPanel;
import io.onedev.server.web.page.project.issues.IssuesPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class MilestoneDetailPage extends IssuesPage {

	private static final String PARAM_MILESTONE = "milestone";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private final IModel<Milestone> milestoneModel;
	
	private String query;
	
	public MilestoneDetailPage(PageParameters params) {
		super(params);
		
		Long milestoneId = params.get(PARAM_MILESTONE).toLong();
		milestoneModel = new LoadableDetachableModel<Milestone>() {

			@Override
			protected Milestone load() {
				return OneDev.getInstance(MilestoneManager.class).load(milestoneId);
			}
			
		};
		
		query = params.get(PARAM_QUERY).toString();
	}

	private Milestone getMilestone() {
		return milestoneModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("name", getMilestone().getName()));
		
		add(new MilestoneStatusLabel("status", milestoneModel));
		
		add(new MilestoneDueLabel("due", milestoneModel));
		
		add(new MilestoneActionsPanel("actions", milestoneModel) {

			@Override
			protected void onDeleted(AjaxRequestTarget target) {
				setResponsePage(MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(getMilestone(), query));
			}

			@Override
			protected void onUpdated(AjaxRequestTarget target) {
				setResponsePage(MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(getMilestone(), query));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
			}

		});
		
		add(new MultilineLabel("description", getMilestone().getDescription()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getMilestone().getDescription() != null);
			}
			
		});
		
		add(new IssueStatsPanel("issueStats", milestoneModel));
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getMilestone(), query);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
				
		add(new IssueListPanel("issues", new PropertyModel<String>(this, "query")) {

			@Override
			protected Project getProject() {
				return MilestoneDetailPage.this.getProject();
			}

			@Override
			protected IssueQuery getBaseQuery() {
				return new IssueQuery(new MilestoneCriteria(getMilestone().getName()), new ArrayList<>());
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target) {
				setResponsePage(MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(getMilestone(), query));
			}

			@Override
			protected QuerySaveSupport getQuerySaveSupport() {
				return null;
			}

		});
	}
	
	@Override
	protected void onDetach() {
		milestoneModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestonesResourceReference()));
	}

	public static PageParameters paramsOf(Milestone milestone, @Nullable String query) {
		PageParameters params = paramsOf(milestone.getProject());
		params.add(PARAM_MILESTONE, milestone.getId());
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
