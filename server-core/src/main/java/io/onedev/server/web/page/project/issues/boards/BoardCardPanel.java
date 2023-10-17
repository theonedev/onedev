package io.onedev.server.web.page.project.issues.boards;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.LinkSide;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener.AttachMode;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.link.IssueLinksPanel;
import io.onedev.server.web.component.issue.milestone.MilestoneCrumbPanel;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.issue.progress.IssueProgressPanel;
import io.onedev.server.web.component.issue.title.IssueTitlePanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Hibernate;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("serial")
public abstract class BoardCardPanel extends GenericPanel<Issue> {
	
	private final Long issueId;
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public BoardCardPanel(String id, Long issueId) {
		super(id);
		this.issueId = issueId;
		setModel(new LoadableDetachableModel<Issue>() {
			@Override
			protected Issue load() {
				return OneDev.getInstance(IssueManager.class).load(issueId);
			}
		});
	}
	
	public Long getIssueId() {
		return issueId;
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
	private Component newContent(String componentId, IModel<Issue> issueModel, @Nullable Cursor cursor) {
		Issue issue = issueModel.getObject();
		
		Fragment fragment = new Fragment(componentId, "contentFrag", this);
		
		BoardSpec board = ((IssueBoardsPage)getPage()).getBoard();

		RepeatingView fieldsView = new RepeatingView("fields");
		for (String fieldName: board.getDisplayFields()) {
			if (fieldName.equals(Issue.NAME_STATE)) {
				Fragment stateFragment = new Fragment(fieldsView.newChildId(),
						"stateFrag", BoardCardPanel.this);
				AjaxLink<Void> transitLink = new TransitionMenuLink("transit") {

					@Override
					protected Issue getIssue() {
						return issueModel.getObject();
					}

				};

				transitLink.add(new IssueStateBadge("state", issueModel));
				stateFragment.add(transitLink);
				fieldsView.add(stateFragment.setOutputMarkupId(true));
			} else if (fieldName.equals(IssueSchedule.NAME_MILESTONE)) {
				fieldsView.add(new MilestoneCrumbPanel(fieldsView.newChildId()) {
					@Override
					protected Issue getIssue() {
						return issueModel.getObject();
					}
				});
			} else {
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
		
		fragment.add(new IssueProgressPanel("progress") {
			@Override
			protected Issue getIssue() {
				return BoardCardPanel.this.getIssue();
			}
		});
		RepeatingView avatarsView = new RepeatingView("avatars");
		for (String fieldName: board.getDisplayFields()) {
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
					}

					@Override
					protected CursorSupport<Issue> getCursorSupport() {
						return new CursorSupport<>() {

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
						BoardCardPanel.this.onDeleteIssue(target);
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
		
		fragment.add(new IssueTitlePanel("numberAndTitle") {

			@Override
			protected Issue getIssue() {
				return issueModel.getObject();
			}

			@Override
			protected Project getCurrentProject() {
				return getProject();
			}

			@Override
			protected Cursor getCursor() {
				return cursor;
			}
			
		});
		
		var linksPanel = new IssueLinksPanel("links") {

			@Override
			protected Issue getIssue() {
				return BoardCardPanel.this.getIssue();
			}

			@Override
			protected List<String> getDisplayLinks() {
				return board.getDisplayLinks();
			}

			@Override
			protected void onToggleExpand(AjaxRequestTarget target) {
				target.add(fragment);
			}
			
		};
		fragment.add(linksPanel);
		
		fragment.add(new ListView<Issue>("linkedIssues", new LoadableDetachableModel<>() {

			@Override
			protected List<Issue> load() {
				Issue issue = issueModel.getObject();
				OneDev.getInstance(IssueLinkManager.class).loadDeepLinks(issue);
				LinkSide side = new LinkSide(linksPanel.getExpandedLink());
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
				setVisible(linksPanel.getExpandedLink() != null);
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
				Hibernate.initialize(issue.getComments());
				Hibernate.initialize(issue.getTargetLinks());
				Hibernate.initialize(issue.getSourceLinks());
				Hibernate.initialize(issue.getMentions());
				for (Milestone milestone: issue.getMilestones())
					Hibernate.initialize(milestone);
				send(getPage(), Broadcast.BREADTH, new IssueDragging(target, issue));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender() {
		replace(newContent("content", getModel(), getCursor()));
		super.onBeforeRender();
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
	
	protected abstract void onDeleteIssue(AjaxRequestTarget target);
	
}
