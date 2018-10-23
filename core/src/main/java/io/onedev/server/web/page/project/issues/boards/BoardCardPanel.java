package io.onedev.server.web.page.project.issues.boards;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Hibernate;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.IssueField;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.issue.IssueStateLabel;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.modal.ModalPanel.Size;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.QueryPositionSupport;

@SuppressWarnings("serial")
abstract class BoardCardPanel extends GenericPanel<Issue> {

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

		List<String> displayFields = ((IssueBoardsPage)getPage()).getBoard().getDisplayFields();
		add(new IssueStateLabel("state", new AbstractReadOnlyModel<Issue>() {

			@Override
			public Issue getObject() {
				return getIssue();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(displayFields.contains(IssueConstants.FIELD_STATE));
			}
			
		});
		
		RepeatingView fieldsView = new RepeatingView("fields");
		for (String fieldName: displayFields) {
			if (!fieldName.equals(IssueConstants.FIELD_STATE)) {
				IssueField field = getIssue().getFields().get(fieldName);
				if (field != null && !field.getType().equals(InputSpec.USER) && !field.getValues().isEmpty()) {
					fieldsView.add(new FieldValuesPanel(fieldsView.newChildId()) {

						@Override
						protected Issue getIssue() {
							return BoardCardPanel.this.getIssue();
						}

						@Override
						protected IssueField getField() {
							return field;
						}
						
					});
				}
			}
		}
		
		add(fieldsView);
		
		RepeatingView avatarsView = new RepeatingView("avatars");
		for (String fieldName: displayFields) {
			IssueField field = getIssue().getFields().get(fieldName);
			if (field != null && field.getType().equals(InputSpec.USER) && !field.getValues().isEmpty()) {
				User user = OneDev.getInstance(UserManager.class)
						.findByName(field.getValues().iterator().next());
				if (user != null) {
					String tooltip = field.getName() + ": " + user.getDisplayName();
					Link<Void> link = new AvatarLink(avatarsView.newChildId(), user, null);
					link.add(AttributeAppender.append("title", tooltip));
					avatarsView.add(link);
				}
			}
		}
		
		add(avatarsView);

		add(new ModalLink("detail", Size.LARGE) {

			private Component newCardDetail(String id, ModalPanel modal, IModel<Issue> issueModel, QueryPosition position) {
				return new CardDetailPanel(id, issueModel) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected QueryPositionSupport<Issue> getQueryPositionSupport() {
						return new QueryPositionSupport<Issue>() {

							@Override
							public QueryPosition getPosition() {
								return position;
							}

							@Override
							public void navTo(AjaxRequestTarget target, Issue entity, QueryPosition position) {
								Long issueId = entity.getId();
								Component cardDetail = newCardDetail(id, modal, new LoadableDetachableModel<Issue>() {

									@Override
									protected Issue load() {
										return OneDev.getInstance(IssueManager.class).load(issueId);
									}
									
								}, position);
								
								replaceWith(cardDetail);
								target.add(cardDetail);
							}
							
						};
					}

					@Override
					protected void onDeletedIssue(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return newCardDetail(id, modal, BoardCardPanel.this.getModel(), getPosition());
			}

		});
		
		Link<Void> link = new BookmarkablePageLink<Void>("number", 
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(getIssue(), getPosition()));
		link.add(new Label("label", "#" + getIssue().getNumber()));
		add(link);

		add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getIssue().getTitle();
			}
			
		}));
		
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

	protected abstract QueryPosition getPosition();
	
}
