package io.onedev.server.web.page.project.issues.milestones;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.MilestoneCriteria;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.util.script.identity.SiteAdministrator;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.milestone.MilestoneDueLabel;
import io.onedev.server.web.component.milestone.MilestoneStatusLabel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class MilestoneDetailPage extends ProjectPage implements ScriptIdentityAware {

	private static final String PARAM_MILESTONE = "milestone";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";
	
	private final IModel<Milestone> milestoneModel;
	
	private String query;
	
	private IssueListPanel issueList;
	
	public MilestoneDetailPage(PageParameters params) {
		super(params);
		
		String idString = params.get(PARAM_MILESTONE).toString();
		if (StringUtils.isBlank(idString))
			throw new RestartResponseException(MilestoneListPage.class, MilestoneListPage.paramsOf(getProject(), false, null));
		
		Long milestoneId = Long.valueOf(idString);
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
		
		add(new MilestoneActionsPanel("actions", milestoneModel, true) {

			@Override
			protected void onDeleted(AjaxRequestTarget target) {
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Milestone.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(MilestoneListPage.class, MilestoneListPage.paramsOf(getProject()));
			}

			@Override
			protected void onUpdated(AjaxRequestTarget target) {
				setResponsePage(MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(getMilestone(), query));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageIssues(getProject()));
			}

		});
		
		add(new MultilineLabel("description", getMilestone().getDescription()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getMilestone().getDescription() != null);
			}
			
		});
		
		add(new StateStatsBar("issueStats", new LoadableDetachableModel<Map<String, Integer>>() {

			@Override
			protected Map<String, Integer> load() {
				return getMilestone().getStateStats();
			}
			
		}) {

			@Override
			protected Link<Void> newStateLink(String componentId, String state) {
				String query = new IssueQuery(new StateCriteria(state)).toString();
				PageParameters params = MilestoneDetailPage.paramsOf(getMilestone(), query);
				return new ViewStateAwarePageLink<Void>(componentId, MilestoneDetailPage.class, params);
			}
			
		});
		
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
				CharSequence url = RequestCycle.get().urlFor(MilestoneDetailPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {
			
		@Override
		protected IssueQuery getBaseQuery() {
			return new IssueQuery(new MilestoneCriteria(getMilestone().getName()), new ArrayList<>());
		}

		@Override
		protected PagingHistorySupport getPagingHistorySupport() {
			return new PagingHistorySupport() {
				
				@Override
				public PageParameters newPageParameters(int currentPage) {
					PageParameters params = paramsOf(getMilestone(), query);
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
			return MilestoneDetailPage.this.getProject();
		}

	});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		add(issueList);
	}
	
	@Override
	protected void onDetach() {
		milestoneModel.detach();
		super.onDetach();
	}

	@Override
	public ScriptIdentity getScriptIdentity() {
		return new SiteAdministrator();
	}
	
	public static PageParameters paramsOf(Milestone milestone, @Nullable String query) {
		PageParameters params = paramsOf(milestone.getProject());
		params.add(PARAM_MILESTONE, milestone.getId());
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("milestones", MilestoneListPage.class, 
				MilestoneListPage.paramsOf(getProject())));
		fragment.add(new Label("milestoneName", getMilestone().getName()));
		return fragment;
	}
	
	@Override
	protected String getPageTitle() {
		return "Milestone " +  getMilestone().getName() + " - " + getProject().getName();
	}
	
}
