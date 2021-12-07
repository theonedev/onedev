package io.onedev.server.web.page.project.issues.boards;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Hibernate;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.LinkSide;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener.AttachMode;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import io.onedev.server.web.util.ReferenceTransformer;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
abstract class BoardCardPanel extends GenericPanel<Issue> {

	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public BoardCardPanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
	private Component newContent(String componentId, IModel<Issue> issueModel, @Nullable Cursor cursor) {
		Issue issue = issueModel.getObject();
		
		Fragment fragment = new Fragment(componentId, "contentFrag", this);
		
		BoardSpec board = ((IssueBoardsPage)getPage()).getBoard();

		List<String> displayFields = board.getDisplayFields();
		
		AjaxLink<Void> transitLink = new TransitionMenuLink("transit") {

			@Override
			protected Issue getIssue() {
				return issueModel.getObject();
			}

			@Override
			protected void onTransited(AjaxRequestTarget target) {
			}
			
		};
		transitLink.setVisible(displayFields.contains(Issue.NAME_STATE));		
		
		transitLink.add(new IssueStateBadge("state", new AbstractReadOnlyModel<Issue>() {

			@Override
			public Issue getObject() {
				return issueModel.getObject();
			}
			
		}));
		
		fragment.add(transitLink);

		RepeatingView fieldsView = new RepeatingView("fields");
		for (String fieldName: displayFields) {
			if (!fieldName.equals(Issue.NAME_STATE)) {
				Input field = issue.getFieldInputs().get(fieldName);
				if (field != null && !field.getType().equals(FieldSpec.USER) && !field.getValues().isEmpty()) {
					fieldsView.add(new FieldValuesPanel(fieldsView.newChildId(), Mode.AVATAR, true) {

						@Override
						protected Issue getIssue() {
							return issueModel.getObject();
						}

						@Override
						protected Input getField() {
							if (issueModel.getObject().isFieldVisible(fieldName))
								return field;
							else
								return null;
						}

						@SuppressWarnings("deprecation")
						@Override
						protected AttachAjaxIndicatorListener getInplaceEditAjaxIndicator() {
							return new AttachAjaxIndicatorListener(
									fieldsView.get(fieldsView.size()-1), AttachMode.APPEND, false);
						}
						
					}.setOutputMarkupId(true));
				}
			}
		}
		
		fragment.add(fieldsView);
		
		RepeatingView avatarsView = new RepeatingView("avatars");
		for (String fieldName: displayFields) {
			Input field = issue.getFieldInputs().get(fieldName);
			if (field != null && field.getType().equals(FieldSpec.USER) && !field.getValues().isEmpty()) {
				avatarsView.add(new FieldValuesPanel(avatarsView.newChildId(), Mode.AVATAR, true) {

					@SuppressWarnings("deprecation")
					@Override
					protected AttachAjaxIndicatorListener getInplaceEditAjaxIndicator() {
						return new AttachAjaxIndicatorListener(avatarsView.get(0), AttachMode.PREPEND, false);
					}

					@Override
					protected Issue getIssue() {
						return issueModel.getObject();
					}

					@Override
					protected Input getField() {
						if (issueModel.getObject().isFieldVisible(fieldName))
							return field;
						else
							return null;
					}
					
				}.setOutputMarkupId(true));
			}
		}
		
		fragment.add(avatarsView);

		BasePage page = (BasePage) getPage();
		
		fragment.add(new ModalLink("showDetail") {

			@Override
			protected String getModalCssClass() {
				return "modal-xl";
			}
			
			private Component newCardDetail(String id, ModalPanel modal, IModel<Issue> issueModel, Cursor cursor) {
				return new CardDetailPanel(id, issueModel) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
						OneDev.getInstance(WebSocketManager.class).observe(page);
					}

					@Override
					protected CursorSupport<Issue> getCursorSupport() {
						return new CursorSupport<Issue>() {

							@Override
							public Cursor getCursor() {
								return cursor;
							}

							@Override
							public void navTo(AjaxRequestTarget target, Issue entity, Cursor cursor) {
								Long issueId = entity.getId();
								Component cardDetail = newCardDetail(id, modal, new LoadableDetachableModel<Issue>() {

									@Override
									protected Issue load() {
										return OneDev.getInstance(IssueManager.class).load(issueId);
									}
									
								}, cursor);
								
								replaceWith(cardDetail);
								target.add(cardDetail);
							}
							
						};
					}

					@Override
					protected void onDeletedIssue(AjaxRequestTarget target) {
						modal.close();
						OneDev.getInstance(WebSocketManager.class).observe(page);
					}

					@Override
					protected void onAfterRender() {
						OneDev.getInstance(WebSocketManager.class).observe(page);
						super.onAfterRender();
					}

					@Override
					protected Project getProject() {
						return BoardCardPanel.this.getProject();
					}

				};
			}
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return newCardDetail(id, modal, BoardCardPanel.this.getModel(), cursor);
			}

		});
		
		ActionablePageLink<Void> numberLink;
		fragment.add(numberLink = new ActionablePageLink<Void>("number", 
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue)) {

			@Override
			public IModel<?> getBody() {
				Issue issue = issueModel.getObject();
				if (getProject().equals(issue.getProject()))
					return Model.of("#" + issue.getNumber());
				else
					return Model.of(issue.getProject() + "#" + issue.getNumber());
			}

			@Override
			protected void doBeforeNav(AjaxRequestTarget target) {
				WebSession.get().setIssueCursor(cursor);
				
				String redirectUrlAfterDelete = RequestCycle.get().urlFor(
						getPage().getClass(), getPage().getPageParameters()).toString();
				WebSession.get().setRedirectUrlAfterDelete(Issue.class, redirectUrlAfterDelete);
			}
			
		});
		
		String url = RequestCycle.get().urlFor(IssueActivitiesPage.class, 
				IssueActivitiesPage.paramsOf(issue)).toString();

		ReferenceTransformer transformer = new ReferenceTransformer(issue.getProject(), url);
		
		fragment.add(new Label("title", Emojis.getInstance().apply(transformer.apply(issue.getTitle()))) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				String script = String.format(""
						+ "$('#%s a:not(.embedded-reference)').click(function() {"
						+ "  $('#%s').click();"
						+ "  return false;"
						+ "});", 
						getMarkupId(), numberLink.getMarkupId());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		}.setEscapeModelStrings(false).setOutputMarkupId(true));
		
		AtomicReference<String> expandedLinkName = new AtomicReference<>(null);
		RepeatingView linksView = new RepeatingView("links");
		
		for (String linkName: board.getDisplayLinks()) {
			int count = 0;
			for (IssueLink link: issue.getTargetLinks()) {
				LinkSpec spec = link.getSpec();
				if (spec.getName().equals(linkName))
					count++;
			}
			for (IssueLink link: issue.getSourceLinks()) {
				LinkSpec spec = link.getSpec();
				if (spec.getOpposite() == null || spec.getOpposite().getName().equals(linkName))
					count++;
			}
			if (count != 0) {
				AjaxLink<Void> link = new AjaxLink<Void>(linksView.newChildId()) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (linkName.equals(expandedLinkName.get()))
							expandedLinkName.set(null);
						else
							expandedLinkName.set(linkName);
						target.add(fragment);
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (linkName.equals(expandedLinkName.get()))
							tag.put("class", tag.getAttribute("class") + " expanded");
					}
					
				};
				link.add(new Label("label", linkName));
				linksView.add(link);
			}
		}
		fragment.add(linksView);
		
		fragment.add(new ListView<Issue>("linkedIssues", new LoadableDetachableModel<List<Issue>>() {

			@Override
			protected List<Issue> load() {
				Issue issue = issueModel.getObject();
				OneDev.getInstance(IssueLinkManager.class).loadDeepLinks(issue);
				LinkSide side = new LinkSide(expandedLinkName.get());
				return issueModel.getObject().findLinkedIssues(side.getSpec(), side.isOpposite());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Issue> item) {
				item.add(newContent("content", item.getModel(), null));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(expandedLinkName.get() != null);
			}
			
		});
		
		fragment.setOutputMarkupId(true);
		
		return fragment;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent("content", getModel(), getCursor()));
		
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
				Project parent = issue.getProject().getParent();
				while (parent != null) {
					Hibernate.initialize(parent);
					parent = parent.getParent();
				}
				Hibernate.initialize(issue.getFields());
				Hibernate.initialize(issue.getSubmitter());
				for (Milestone milestone: issue.getMilestones())
					Hibernate.initialize(milestone);
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

	protected abstract Cursor getCursor();
	
	protected abstract Project getProject();
	
}
