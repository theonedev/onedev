package io.onedev.server.web.page.project.issues.boards;

import static io.onedev.server.security.SecurityUtils.canAccessIssue;
import static io.onedev.server.security.SecurityUtils.canManageIssues;
import static io.onedev.server.security.SecurityUtils.getAuthUser;
import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Hibernate;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.util.LinkDescriptor;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener.AttachMode;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.iteration.IterationCrumbPanel;
import io.onedev.server.web.component.issue.link.IssueLinksPanel;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.issue.progress.IssueProgressPanel;
import io.onedev.server.web.component.issue.title.IssueTitlePanel;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;

public abstract class BoardCardPanel extends GenericPanel<Issue> {
	
	private final Long issueId;
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public BoardCardPanel(String id, Long issueId) {
		super(id);
		this.issueId = issueId;
		setModel(new LoadableDetachableModel<>() {
			@Override
			protected Issue load() {
				return OneDev.getInstance(IssueService.class).load(issueId);
			}
		});
	}
	
	public Long getIssueId() {
		return issueId;
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
	private Component newContent(String componentId, IModel<Issue> issueModel, Set<Long> displayedIssueIds) {
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

				transitLink.add(new IssueStateBadge("state", issueModel, false));
				stateFragment.add(transitLink);
				fieldsView.add(stateFragment.setOutputMarkupId(true));
			} else if (fieldName.equals(IssueSchedule.NAME_ITERATION)) {
				fieldsView.add(new IterationCrumbPanel(fieldsView.newChildId()) {
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
										return OneDev.getInstance(IssueService.class).load(issueId);
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
				return newCardDetail(id, modal, BoardCardPanel.this.getModel(), getCursor());
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
				return BoardCardPanel.this.getCursor();
			}
			
		});
		
		fragment.add(new CopyToClipboardLink("copy", 
				Model.of(issue.getTitle() + " (" + issue.getReference().toString(getProject()) + ")")));

		var linksPanel = new IssueLinksPanel("links") {

			@Override
			protected Issue getIssue() {
				return issueModel.getObject();
			}

			@Override
			protected List<String> getDisplayLinks() {
				if (displayedIssueIds.contains(getIssue().getId()))
					return Collections.emptyList();
				else
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
				OneDev.getInstance(IssueLinkService.class).loadDeepLinks(issue);
				LinkDescriptor descriptor = new LinkDescriptor(linksPanel.getExpandedLink());
				return issue.findLinkedIssues(descriptor.getSpec(), descriptor.isOpposite()).stream().filter(it->canAccessIssue(it)).collect(toList());
			}

		}) {

			@Override
			protected void populateItem(ListItem<Issue> item) {
				var copyOfDisplayedIssueIds = new HashSet<>(displayedIssueIds);
				copyOfDisplayedIssueIds.add(issueModel.getObject().getId());
				item.add(newContent("content", item.getModel(), copyOfDisplayedIssueIds));
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
		
		add(newContent("content", getModel(), new HashSet<>()));
		
		add(AttributeAppender.append("data-issue", getIssue().getId()));
		
		if (getAuthUser() != null)
			add(AttributeAppender.append("style", "cursor:move;"));
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				if (canManageIssues(getProject())) {
					Long issueId = RequestCycle.get().getRequest().getPostParameters()
							.getParameterValue("issue").toLong();
					Issue issue = OneDev.getInstance(IssueService.class).load(issueId);
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
					for (Iteration iteration : issue.getIterations())
						Hibernate.initialize(iteration);
					send(getPage(), Broadcast.BREADTH, new IssueDragging(target, issue));
				} else {
					Session.get().warn(_T("Issue management permission required to move issues"));
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender() {
		replace(newContent("content", getModel(), new HashSet<>()));
		super.onBeforeRender();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		CharSequence callback = ajaxBehavior.getCallbackFunction(CallbackParameter.explicit("issue"));
		String script = String.format("onedev.server.issueBoards.onCardDomReady('%s', %s);", 
				getMarkupId(), getAuthUser() != null? callback: "undefined");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected abstract Cursor getCursor();
	
	protected abstract Project getProject();
	
	protected abstract void onDeleteIssue(AjaxRequestTarget target);
	
}
