package io.onedev.server.web.page.project.issues.issueboards;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.IssueQueryLexer;
import io.onedev.server.model.support.issue.query.MilestoneUnaryCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

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
				criterias.add(new MilestoneUnaryCriteria(IssueQueryLexer.IsEmpty));
				return new IssueQuery(IssueCriteria.of(criterias), backlogQuery.getSorts());
			} else {
				return null;
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

					@Override
					protected void onAdded(AjaxRequestTarget target, Issue issue) {
						target.add(BacklogColumnPanel.this);
					}
					
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuery() != null && SecurityUtils.getUser() != null);
			}
			
		});
		add(new Label("count", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(OneDev.getInstance(IssueManager.class)
						.count(getProject(), getQuery().getCriteria()));
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuery() != null);
			}
			
		});
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				Issue issue = OneDev.getInstance(IssueManager.class).load(params.getParameterValue("issue").toLong());
				if (!SecurityUtils.canModify(issue)) 
					throw new UnauthorizedException("Permission denied");
				OneDev.getInstance(IssueChangeManager.class).changeMilestone(issue, null);
				if (getQuery().matches(issue)) {
					target.add(BacklogColumnPanel.this);
				} else {
					new ModalPanel(target) {

						@Override
						protected Component newContent(String id) {
							return new CardUnmatchedPanel(id) {
								
								@Override
								protected void onClose(AjaxRequestTarget target) {
									close();
								}
								
								@Override
								protected Issue getIssue() {
									return issue;
								}
								
							};
						}
						
					};
				}
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
				if (getQuery() != null && event.getPayload() instanceof IssueDragging) {
					IssueDragging issueDragging = (IssueDragging) event.getPayload();
					Issue issue = issueDragging.getIssue();
					if (SecurityUtils.canModify(issue)) {
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
			protected List<Issue> queryIssues(int offset, int count) {
				if (getQuery() != null) 
					return OneDev.getInstance(IssueManager.class).query(getProject(), getQuery(), offset, count);
				else 
					return new ArrayList<>();
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				CharSequence callback = ajaxBehavior.getCallbackFunction(CallbackParameter.explicit("issue"));
				String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s', %s);", 
						getMarkupId(), getQuery()!=null?callback:"undefined");
				response.render(OnDomReadyHeaderItem.forScript(script));
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
		super.onDetach();
	}

	protected abstract Project getProject();
	
	@Nullable
	protected abstract IssueQuery getBacklogQuery();
	
}
