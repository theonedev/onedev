package io.onedev.server.web.page.project.issues.boards;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.MilestoneCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.create.CreateIssuePanel;
import io.onedev.server.web.component.issue.progress.QueriedIssuesProgressPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.WicketUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
abstract class BacklogColumnPanel extends Panel {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			IssueQuery backlogQuery = getBacklogQuery();
			if (backlogQuery != null) {
				List<Criteria<Issue>> criterias = new ArrayList<>();
				if (backlogQuery.getCriteria() != null)
					criterias.add(backlogQuery.getCriteria());
				criterias.add(new NotCriteria<Issue>(new MilestoneCriteria(getMilestone().getName())));
				return new IssueQuery(Criteria.andCriterias(criterias), backlogQuery.getSorts());
			} else {
				return null;
			}
		}
		
	};

	private final IModel<Integer> countModel = new LoadableDetachableModel<>() {

		@Override
		protected Integer load() {
			if (getQuery() != null) {
				try {
					return getIssueManager().count(getProjectScope(), getQuery().getCriteria());
				} catch (ExplicitException e) {
					return 0;
				}
			} else {
				return 0;
			}
		}

	};
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	private Component countLabel;
	
	public BacklogColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ModalLink("addCard") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new CreateIssuePanel(id) {

					@Override
					protected void onSave(AjaxRequestTarget target, Issue issue) {
						getIssueManager().open(issue);
						notifyIssueChange(target, issue);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected Project getProject() {
						return BacklogColumnPanel.this.getProject();
					}

					@Override
					protected Criteria<Issue> getTemplate() {
						return getQuery().getCriteria();
					}

				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuery() != null && SecurityUtils.getUser() != null);
			}
			
		});

		if (getQuery() != null && getProject().isTimeTracking() && WicketUtils.isSubscriptionActive()) {
			add(new DropdownLink("showProgress") {
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new QueriedIssuesProgressPanel(id) {
						@Override
						protected ProjectScope getProjectScope() {
							return BacklogColumnPanel.this.getProjectScope();
						}

						@Override
						protected IssueQuery getQuery() {
							return BacklogColumnPanel.this.getQuery();
						}
					};
				}
			});
		} else {
			add(new WebMarkupContainer("showProgress").setVisible(false));
		}
		
		if (getQuery() != null) {
			PageParameters params = ProjectIssueListPage.paramsOf(getProject(), getQuery().toString(), 0);
			add(new BookmarkablePageLink<Void>("viewAsList", ProjectIssueListPage.class, params));
		} else {
			add(new WebMarkupContainer("viewAsList").setVisible(false));
		}
		
		add(countLabel = new Label("count", countModel).setOutputMarkupId(true));
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				Issue issue = getIssueManager().load(params.getParameterValue("issue").toLong());
				if (!SecurityUtils.canScheduleIssues(issue.getProject())) 
					throw new UnauthorizedException("Permission denied");
				getIssueChangeManager().removeSchedule(issue, getMilestone());
				notifyIssueChange(target, issue);
				target.appendJavaScript("$('.issue-boards').data('accepted', true);");
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		addOrReplace(new CardListPanel("body") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof IssueDragging && getQuery() != null) {
					IssueDragging issueDragging = (IssueDragging) event.getPayload();
					Issue issue = issueDragging.getIssue();
					if (SecurityUtils.canScheduleIssues(issue.getProject())) {
						issue = SerializationUtils.clone(issue);
						issue.removeSchedule(getMilestone());
					}
					if (getQuery().matches(issue)) {
						String script = String.format("$('#%s').addClass('issue-droppable');", getMarkupId());
						issueDragging.getHandler().appendJavaScript(script);
					}
				}
				event.dontBroadcastDeeper();
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				CharSequence callback = ajaxBehavior.getCallbackFunction(CallbackParameter.explicit("issue"));
				String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s', %s);", 
						getMarkupId(), getQuery()!=null?callback:"undefined");
				// Use OnLoad instead of OnDomReady as otherwise perfect scrollbar is not shown unless resized 
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

			@Override
			protected ProjectScope getProjectScope() {
				return BacklogColumnPanel.this.getProjectScope();
			}

			@Override
			protected IssueQuery getQuery() {
				return BacklogColumnPanel.this.getQuery();
			}

			@Override
			protected int getCardCount() {
				return countModel.getObject();
			}

			@Override
			protected void onUpdate(IPartialPageRequestHandler handler) {
				handler.add(countLabel);
			}

		});
		
		super.onBeforeRender();
	}
	
	private IssueQuery getQuery() {
		return queryModel.getObject();
	}

	@Override
	protected void onDetach() {
		queryModel.detach();
		countModel.detach();
		super.onDetach();
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
	}
	
	private Project getProject() {
		return getProjectScope().getProject();
	}

	protected abstract ProjectScope getProjectScope();
	
	@Nullable
	protected abstract IssueQuery getBacklogQuery();
	
	protected abstract Milestone getMilestone();
	
	private void notifyIssueChange(AjaxRequestTarget target, Issue issue) {
		((BasePage)getPage()).notifyObservablesChange(target, issue.getChangeObservables(true));
	}
	
}
