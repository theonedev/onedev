package io.onedev.server.web.page.project.issues.boards;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.MilestoneIsEmptyCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;

@SuppressWarnings("serial")
abstract class BacklogColumnPanel extends Panel {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			IssueQuery backlogQuery = getBacklogQuery();
			if (backlogQuery != null) {
				List<IssueCriteria> criterias = new ArrayList<>();
				if (backlogQuery.getCriteria() != null)
					criterias.add(backlogQuery.getCriteria());
				criterias.add(new MilestoneIsEmptyCriteria());
				return new IssueQuery(IssueCriteria.and(criterias), backlogQuery.getSorts());
			} else {
				return null;
			}
		}
		
	};

	private final IModel<Integer> countModel = new LoadableDetachableModel<Integer>() {

		@Override
		protected Integer load() {
			if (getQuery() != null) {
				try {
					return OneDev.getInstance(IssueManager.class).count(getProject(), getQuery().getCriteria());
				} catch (ExplicitException e) {
					return 0;
				}
			} else {
				return 0;
			}
		}
		
	};
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public BacklogColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ModalLink("addCard") {

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new NewCardPanel(id) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected Project getProject() {
						return BacklogColumnPanel.this.getProject();
					}

					@Override
					protected IssueCriteria getTemplate() {
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
		
		if (getQuery() != null) {
			PageParameters params = ProjectIssueListPage.paramsOf(getProject(), getQuery().toString(), 0);
			add(new BookmarkablePageLink<Void>("viewAsList", ProjectIssueListPage.class, params));
		} else {
			add(new WebMarkupContainer("viewAsList").setVisible(false));
		}
		
		add(new CardCountLabel("count") {

			@Override
			protected Project getProject() {
				return BacklogColumnPanel.this.getProject();
			}

			@Override
			protected int getCount() {
				return countModel.getObject();
			}

		});
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				Issue issue = OneDev.getInstance(IssueManager.class).load(params.getParameterValue("issue").toLong());
				if (!SecurityUtils.canScheduleIssues(issue.getProject())) 
					throw new UnauthorizedException("Permission denied");
				OneDev.getInstance(IssueChangeManager.class).changeMilestone(issue, null);
				target.appendJavaScript(String.format("onedev.server.issueBoards.markAccepted(%d, true);", issue.getId()));
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
						issue.setMilestone(null);
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
			protected Project getProject() {
				return BacklogColumnPanel.this.getProject();
			}

			@Override
			protected IssueQuery getQuery() {
				return BacklogColumnPanel.this.getQuery();
			}

			@Override
			protected int getCardCount() {
				return countModel.getObject();
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

	protected abstract Project getProject();
	
	@Nullable
	protected abstract IssueQuery getBacklogQuery();
	
}
