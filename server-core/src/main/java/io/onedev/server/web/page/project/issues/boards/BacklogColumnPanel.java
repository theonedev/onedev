package io.onedev.server.web.page.project.issues.boards;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IterationCriteria;
import io.onedev.server.search.entity.issue.IterationEmptyCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.create.CreateIssuePanel;
import io.onedev.server.web.component.issue.progress.QueriedIssuesProgressPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.WicketUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.onedev.server.search.entity.issue.IssueQueryLexer.IsEmpty;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.IsNot;
import static io.onedev.server.security.SecurityUtils.canManageIssues;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

abstract class BacklogColumnPanel extends AbstractColumnPanel {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected IssueQuery load() {
			IssueQuery backlogQuery = getBacklogQuery();
			if (backlogQuery != null) {
				List<Criteria<Issue>> criterias = new ArrayList<>();
				if (backlogQuery.getCriteria() != null)
					criterias.add(backlogQuery.getCriteria());
				if (getIterationPrefix() != null)
					criterias.add(new IterationCriteria(getIterationPrefix() + "*", IsNot));
				else
					criterias.add(new IterationEmptyCriteria(IsEmpty));					
				return new IssueQuery(Criteria.andCriterias(criterias), backlogQuery.getSorts());
			} else {
				return null;
			}
		}

	};
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	private Component countLabel;
	
	private Component addToIterationLink;
	
	private CardListPanel cardListPanel;
	
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
						onCardAdded(target, issue);
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
				setVisible(getQuery() != null && SecurityUtils.getAuthUser() != null);
			}
			
		});
		
		add(addToIterationLink = newAddToIterationLink("addToIteration"));

		if (getQuery() != null && getProject().isTimeTracking() 
				&& WicketUtils.isSubscriptionActive() 
				&& SecurityUtils.canAccessTimeTracking(getProject())) {
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
				var subject = SecurityUtils.getSubject();
				if (!canManageIssues(subject, getProject()))
					throw new UnauthorizedException(_T("Permission denied"));
				
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				var issueId = params.getParameterValue("issueId").toLong();
				var cardIndex = params.getParameterValue("cardIndex").toInt();
				
				var card = cardListPanel.findCard(issueId);
				if (card == null) { // moved from other columns
					var issue = getIssueService().load(issueId);
					var user = SecurityUtils.getUser(subject);
					for (var iteration: getProject().getHierarchyIterations()) {
						if (getIterationPrefix() == null || iteration.getName().startsWith(getIterationPrefix()))
							getIssueChangeService().removeSchedule(user, issue, iteration);
					}
				}
				cardListPanel.onCardDropped(target, issueId, cardIndex, true);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		addOrReplace(cardListPanel = new CardListPanel("body") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof IssueDragging && getQuery() != null) {
					IssueDragging issueDragging = (IssueDragging) event.getPayload();
					Issue issue = issueDragging.getIssue();
					if (SecurityUtils.canScheduleIssues(issue.getProject())) {
						issue = SerializationUtils.clone(issue);
						for (var iteration: getProject().getHierarchyIterations()) {
							if (getIterationPrefix() == null || iteration.getName().startsWith(getIterationPrefix()))
								issue.removeSchedule(iteration);
						}
						issue.getLastActivity().setDate(new Date());
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
				CharSequence callback = ajaxBehavior.getCallbackFunction(
						explicit("issueId"), explicit("cardIndex"));
				String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s', %s);", 
						getMarkupId(), (getQuery() != null && canManageIssues(getProject()))? callback:"undefined");
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
			protected void updateCardCount(IPartialPageRequestHandler handler) {
				handler.add(countLabel);
				if (addToIterationLink.getOutputMarkupId())
					handler.add(addToIterationLink);
			}

		});
		
		super.onBeforeRender();
	}

	@Override
	protected CardListPanel getCardListPanel() {
		return cardListPanel;
	}
	
	@Override
	protected IssueQuery getQuery() {
		return queryModel.getObject();
	}

	@Override
	protected void onDetach() {
		queryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract IssueQuery getBacklogQuery();

	@Override
	protected boolean isBacklog() {
		return true;
	}
	
}
