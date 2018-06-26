package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Hibernate;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;

@SuppressWarnings("serial")
class BoardCardPanel extends GenericPanel<Issue> {

	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public BoardCardPanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("body", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getIssue().getTitle();
			}
			
		}));
		
		RepeatingView avatarsView = new RepeatingView("avatars");
		for (IssueField field: getIssue().getFields().values()) {
			if (field.getType().equals(InputSpec.USER_CHOICE) && !field.getValues().isEmpty()) {
				User user = OneDev.getInstance(UserManager.class)
						.findByName(field.getValues().iterator().next());
				if (user != null) {
					String tooltip = field.getName() + ": " + user.getDisplayName();
					avatarsView.add(new AvatarLink(avatarsView.newChildId(), user, tooltip));
				}
			}
		}
		
		add(avatarsView);

		Link<Void> link = new BookmarkablePageLink<Void>("number", 
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(getIssue(), null));
		link.add(new Label("label", "#" + getIssue().getNumber()));
		add(link);
		
		add(new WebMarkupContainer("state") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				String state = getIssue().getState();
				StateSpec stateSpec = getIssue().getProject().getIssueWorkflow().getStateSpec(state);
				add(AttributeAppender.append("style", "background:" + stateSpec.getColor() + ";"));
				add(AttributeAppender.append("title", state));
			}
			
		});
		
		add(new ModalLink("detail") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new CardDetailPanel(id, BoardCardPanel.this.getModel()) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
					}
					
				};
			}

		});
		
		add(AttributeAppender.append("data-issue", getIssue().getId()));
		
		if (SecurityUtils.getUser() != null)
			add(AttributeAppender.append("style", "cursor:move;"));
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				Long issueId = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("issue").toLong();
				Issue issue = OneDev.getInstance(IssueManager.class).load(issueId);
				Hibernate.initialize(issue.getProject());
				Hibernate.initialize(issue.getMilestone());
				Hibernate.initialize(issue.getFieldUnaries());
				Hibernate.initialize(issue.getSubmitter());
				send(getPage(), Broadcast.BREADTH, new IssueDragging(target, issue));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		CharSequence callback = ajaxBehavior.getCallbackFunction(CallbackParameter.explicit("issue"));
		String script = String.format("onedev.server.issueBoards.onCardDomReady('%s', %s);", 
				getMarkupId(), SecurityUtils.getUser()!=null?callback:"undefined");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
